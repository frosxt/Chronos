package com.github.frosxt.chronos.runtime.cron;

import com.github.frosxt.chronos.api.cron.CronExpression;
import com.github.frosxt.chronos.runtime.cron.model.CronFields;
import com.github.frosxt.chronos.runtime.cron.search.DayMatcher;
import com.github.frosxt.chronos.runtime.cron.search.NextFieldSearch;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Calculates the next fire time for a cron expression.
 *
 * <p>
 * This calculator respects timezone boundaries and uses ZonedDateTime
 * to respect timezone/DST rules.
 */
public final class CronNextFireCalculator {

    private CronNextFireCalculator() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Calculates the next fire time after the given reference time.
     *
     * @param cron      the cron expression
     * @param zone      the timezone for calculations
     * @param reference the reference time
     * @return the next fire time, or null if none can be computed within 4 years
     */
    public static ZonedDateTime nextFire(final CronExpression cron, final ZoneId zone, final ZonedDateTime reference) {
        return nextFire(new CronFields(cron), zone, reference);
    }

    /**
     * Internal calculation using CronFields model.
     */
    private static ZonedDateTime nextFire(final CronFields fields, final ZoneId zone, final ZonedDateTime reference) {
        ZonedDateTime candidate = reference.plusMinutes(1)
                .withSecond(0)
                .withNano(0);

        final int maxIterations = 366 * 4;

        for (int i = 0; i < maxIterations; i++) {
            if (!NextFieldSearch.isInArray(candidate.getMonthValue(), fields.months())) {
                candidate = advanceToNextMonth(candidate, fields.months());
                continue;
            }

            if (!DayMatcher.isValidDayOfMonth(candidate.getDayOfMonth(), fields.daysOfMonth(), fields.daysOfWeek(), candidate)) {
                candidate = candidate.plusDays(1).withHour(0).withMinute(0);
                continue;
            }

            if (!NextFieldSearch.isInArray(candidate.getHour(), fields.hours())) {
                final int nextHour = NextFieldSearch.findNext(candidate.getHour(), fields.hours());
                if (nextHour == -1) {
                    candidate = candidate.plusDays(1).withHour(0).withMinute(0);
                } else {
                    candidate = candidate.withHour(nextHour).withMinute(0);
                }
                continue;
            }

            if (!NextFieldSearch.isInArray(candidate.getMinute(), fields.minutes())) {
                final int nextMinute = NextFieldSearch.findNext(candidate.getMinute(), fields.minutes());
                if (nextMinute == -1) {
                    candidate = candidate.plusHours(1).withMinute(0);
                } else {
                    candidate = candidate.withMinute(nextMinute);
                }
                continue;
            }

            return candidate;
        }

        return null;
    }

    private static ZonedDateTime advanceToNextMonth(final ZonedDateTime dt, final int[] months) {
        final int currentMonth = dt.getMonthValue();
        final int nextMonth = NextFieldSearch.findNext(currentMonth, months);

        if (nextMonth != -1) {
            return dt.withMonth(nextMonth).withDayOfMonth(1).withHour(0).withMinute(0);
        }

        final int firstMonth = months[0];
        return dt.plusYears(1).withMonth(firstMonth).withDayOfMonth(1).withHour(0).withMinute(0);
    }
}
