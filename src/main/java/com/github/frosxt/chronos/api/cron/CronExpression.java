package com.github.frosxt.chronos.api.cron;

import java.util.Arrays;

/**
 * Represents a parsed cron expression for schedule definition.
 *
 * <p>
 * Cron expressions use a 5-field format:
 *
 * <pre>
 * minute hour day-of-month month day-of-week
 * </pre>
 *
 * <p>
 * Supported syntax:
 * <ul>
 * <li>{@code *} - all values</li>
 * <li>{@code 2-5} - range of values</li>
 * <li>{@code 1,2,3} - list of values</li>
 * <li>{@code * / 5} or {@code 1-10/2} - step values</li>
 * </ul>
 *
 * <p>
 * Day-of-week accepts 0-7 where both 0 and 7 represent Sunday.
 * When both day-of-month and day-of-week are restricted, a match on either
 * field is sufficient.
 *
 * <p>
 * This class is immutable and thread-safe.
 */
public record CronExpression(String expression, int[] minutes, int[] hours, int[] daysOfMonth, int[] months, int[] daysOfWeek) {

    /**
     * Creates a new CronExpression from parsed field values.
     *
     * @param expression  the original expression string
     * @param minutes     allowed minute values
     * @param hours       allowed hour values
     * @param daysOfMonth allowed day-of-month values
     * @param months      allowed month values
     * @param daysOfWeek  allowed day-of-week values
     */
    public CronExpression(final String expression, final int[] minutes, final int[] hours,
                          final int[] daysOfMonth, final int[] months, final int[] daysOfWeek) {
        this.expression = expression;

        this.minutes = minutes.clone();
        Arrays.sort(this.minutes);

        this.hours = hours.clone();
        Arrays.sort(this.hours);

        this.daysOfMonth = daysOfMonth.clone();
        Arrays.sort(this.daysOfMonth);

        this.months = months.clone();
        Arrays.sort(this.months);

        this.daysOfWeek = daysOfWeek.clone();
        Arrays.sort(this.daysOfWeek);
    }

    /**
     * Parses a cron expression string into a {@code CronExpression}.
     *
     * @param expression the cron expression to parse
     * @return the parsed cron expression
     * @throws NullPointerException     if expression is null
     * @throws IllegalArgumentException if the expression is invalid
     */
    public static CronExpression parse(final String expression) {
        if (expression == null) {
            throw new NullPointerException("expression must not be null");
        }

        final String trimmed = expression.trim();
        final String[] parts = trimmed.split("\\s+");

        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid cron expression: expected 5 fields, got " + parts.length);
        }

        final int[] minuteArr = parseField(parts[0], 0, 59, "minute");
        final int[] hourArr = parseField(parts[1], 0, 23, "hour");
        final int[] domArr = parseField(parts[2], 1, 31, "day-of-month");
        final int[] monthArr = parseField(parts[3], 1, 12, "month");
        final int[] dowArr = parseDayOfWeek(parts[4]);

        return new CronExpression(trimmed, minuteArr, hourArr, domArr, monthArr, dowArr);
    }

    private static int[] parseField(final String field, final int min, final int max, final String name) {
        final boolean[] allowed = new boolean[max - min + 1];

        final String[] listParts = field.split(",");
        for (final String part : listParts) {
            parsePart(part, allowed, min, max, name);
        }

        return toArray(allowed, min);
    }

    private static void parsePart(final String part, final boolean[] allowed, final int min, final int max, final String name) {
        if (part.equals("*")) {
            Arrays.fill(allowed, true);
            return;
        }

        if (part.contains("/")) {
            parseStep(part, allowed, min, max, name);
            return;
        }

        if (part.contains("-")) {
            parseRange(part, allowed, min, max, name);
            return;
        }

        final int value = parseNumber(part, name);
        validateRange(value, min, max, name);
        allowed[value - min] = true;
    }

    private static void parseStep(final String part, final boolean[] allowed, final int min, final int max, final String name) {
        final String[] stepParts = part.split("/", 2);
        if (stepParts.length != 2) {
            throw new IllegalArgumentException("Invalid step in " + name + " field: " + part);
        }

        final int step = parseNumber(stepParts[1], name + " step");
        if (step < 1) {
            throw new IllegalArgumentException("Step must be positive in " + name + " field");
        }

        final int start;
        int end = max;

        if (stepParts[0].equals("*")) {
            start = min;
        } else if (stepParts[0].contains("-")) {
            final String[] rangeParts = stepParts[0].split("-", 2);
            start = parseNumber(rangeParts[0], name);
            end = parseNumber(rangeParts[1], name);
            validateRange(start, min, max, name);
            validateRange(end, min, max, name);
            if (start > end) {
                throw new IllegalArgumentException("Invalid range in " + name + " field: " + stepParts[0]);
            }
        } else {
            start = parseNumber(stepParts[0], name);
            validateRange(start, min, max, name);
        }

        for (int v = start; v <= end; v += step) {
            allowed[v - min] = true;
        }
    }

    private static void parseRange(final String part, final boolean[] allowed, final int min, final int max, final String name) {
        final String[] rangeParts = part.split("-", 2);
        final int start = parseNumber(rangeParts[0], name);
        final int end = parseNumber(rangeParts[1], name);

        validateRange(start, min, max, name);
        validateRange(end, min, max, name);

        if (start > end) {
            throw new IllegalArgumentException("Invalid range in " + name + " field: " + part);
        }

        for (int v = start; v <= end; v++) {
            allowed[v - min] = true;
        }
    }

    private static int[] parseDayOfWeek(final String field) {
        final boolean[] allowed = new boolean[7];

        final String[] listParts = field.split(",");
        for (final String part : listParts) {
            parseDowPart(part, allowed);
        }

        return toArray(allowed, 0);
    }

    private static void parseDowPart(final String part, final boolean[] allowed) {
        if (part.equals("*")) {
            Arrays.fill(allowed, true);
            return;
        }

        if (part.contains("/")) {
            parseDowStep(part, allowed);
            return;
        }

        if (part.contains("-")) {
            parseDowRange(part, allowed);
            return;
        }

        int value = parseNumber(part, "day-of-week");
        value = normalizeDow(value);
        allowed[value] = true;
    }

    private static void parseDowStep(final String part, final boolean[] allowed) {
        final String[] stepParts = part.split("/", 2);
        if (stepParts.length != 2) {
            throw new IllegalArgumentException("Invalid step in day-of-week field: " + part);
        }

        final int step = parseNumber(stepParts[1], "day-of-week step");
        if (step < 1) {
            throw new IllegalArgumentException("Step must be positive in day-of-week field");
        }

        final int start;
        int end = 6;

        if (stepParts[0].equals("*")) {
            start = 0;
        } else if (stepParts[0].contains("-")) {
            final String[] rangeParts = stepParts[0].split("-", 2);
            start = normalizeDow(parseNumber(rangeParts[0], "day-of-week"));
            end = normalizeDow(parseNumber(rangeParts[1], "day-of-week"));
            if (start > end) {
                throw new IllegalArgumentException("Invalid range in day-of-week field: " + stepParts[0]);
            }
        } else {
            start = normalizeDow(parseNumber(stepParts[0], "day-of-week"));
        }

        for (int v = start; v <= end; v += step) {
            allowed[v] = true;
        }
    }

    private static void parseDowRange(final String part, final boolean[] allowed) {
        final String[] rangeParts = part.split("-", 2);
        final int start = normalizeDow(parseNumber(rangeParts[0], "day-of-week"));
        final int end = normalizeDow(parseNumber(rangeParts[1], "day-of-week"));

        if (start > end) {
            throw new IllegalArgumentException("Invalid range in day-of-week field: " + part);
        }

        for (int v = start; v <= end; v++) {
            allowed[v] = true;
        }
    }

    private static int normalizeDow(final int dow) {
        if (dow < 0 || dow > 7) {
            throw new IllegalArgumentException("day-of-week must be 0-7, got: " + dow);
        }
        return dow == 7 ? 0 : dow;
    }

    private static int parseNumber(final String str, final String fieldName) {
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in " + fieldName + " field: " + str);
        }
    }

    private static void validateRange(final int value, final int min, final int max, final String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max + ", got: " + value);
        }
    }

    private static int[] toArray(final boolean[] allowed, final int min) {
        int count = 0;
        for (final boolean b : allowed) {
            if (b) {
                count++;
            }
        }

        final int[] result = new int[count];
        int idx = 0;
        for (int i = 0; i < allowed.length; i++) {
            if (allowed[i]) {
                result[idx++] = i + min;
            }
        }
        return result;
    }

    /**
     * Returns the original cron expression string.
     *
     * @return the expression string, never null
     */
    @Override
    public String expression() {
        return expression;
    }

    /**
     * Returns the allowed minutes (0-59).
     *
     * @return array of allowed minute values
     */
    @Override
    public int[] minutes() {
        return minutes.clone();
    }

    /**
     * Returns the allowed hours (0-23).
     *
     * @return array of allowed hour values
     */
    @Override
    public int[] hours() {
        return hours.clone();
    }

    /**
     * Returns the allowed days of month (1-31).
     *
     * @return array of allowed day-of-month values
     */
    @Override
    public int[] daysOfMonth() {
        return daysOfMonth.clone();
    }

    /**
     * Returns the allowed months (1-12).
     *
     * @return array of allowed month values
     */
    @Override
    public int[] months() {
        return months.clone();
    }

    /**
     * Returns the allowed days of week (0-6, where 0 is Sunday).
     *
     * @return array of allowed day-of-week values
     */
    @Override
    public int[] daysOfWeek() {
        return daysOfWeek.clone();
    }

    @Override
    public String toString() {
        return "CronExpression[" + expression + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final CronExpression other)) {
            return false;
        }
        return expression.equals(other.expression);
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }
}
