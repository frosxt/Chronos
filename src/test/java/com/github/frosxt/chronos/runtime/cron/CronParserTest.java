package com.github.frosxt.chronos.runtime.cron;

import com.github.frosxt.chronos.api.cron.CronExpression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CronParser}.
 */
class CronParserTest {

    @Test
    void parseSimpleExpression() {
        CronExpression expr = CronExpression.parse("0 0 * * *");

        assertArrayEquals(new int[] { 0 }, expr.minutes());
        assertArrayEquals(new int[] { 0 }, expr.hours());
        assertEquals(31, expr.daysOfMonth().length);
        assertEquals(12, expr.months().length);
        assertEquals(7, expr.daysOfWeek().length);
    }

    @Test
    void parseAllWildcards() {
        CronExpression expr = CronExpression.parse("* * * * *");

        assertEquals(60, expr.minutes().length);
        assertEquals(24, expr.hours().length);
        assertEquals(31, expr.daysOfMonth().length);
        assertEquals(12, expr.months().length);
        assertEquals(7, expr.daysOfWeek().length);
    }

    @Test
    void parseRange() {
        CronExpression expr = CronExpression.parse("0-5 * * * *");

        assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5 }, expr.minutes());
    }

    @Test
    void parseList() {
        CronExpression expr = CronExpression.parse("0,15,30,45 * * * *");

        assertArrayEquals(new int[] { 0, 15, 30, 45 }, expr.minutes());
    }

    @Test
    void parseStep() {
        CronExpression expr = CronExpression.parse("*/15 * * * *");

        assertArrayEquals(new int[] { 0, 15, 30, 45 }, expr.minutes());
    }

    @Test
    void parseRangeWithStep() {
        CronExpression expr = CronExpression.parse("0-30/10 * * * *");

        assertArrayEquals(new int[] { 0, 10, 20, 30 }, expr.minutes());
    }

    @Test
    void parseDayOfWeekNormalization() {
        CronExpression expr = CronExpression.parse("0 0 * * 7");
        assertArrayEquals(new int[] { 0 }, expr.daysOfWeek());

        CronExpression expr2 = CronExpression.parse("0 0 * * 0");
        assertArrayEquals(new int[] { 0 }, expr2.daysOfWeek());
    }

    @Test
    void parseComplexExpression() {
        CronExpression expr = CronExpression.parse("0,30 9-17 * 1-6 1-5");

        assertArrayEquals(new int[] { 0, 30 }, expr.minutes());
        assertArrayEquals(new int[] { 9, 10, 11, 12, 13, 14, 15, 16, 17 }, expr.hours());
        assertEquals(31, expr.daysOfMonth().length);
        assertArrayEquals(new int[] { 1, 2, 3, 4, 5, 6 }, expr.months());
        assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, expr.daysOfWeek());
    }

    @Test
    void parseInvalidFieldCount() {
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("0 0 * *"));

        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("0 0 * * * *"));
    }

    @Test
    void parseInvalidMinute() {
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("60 * * * *"));
    }

    @Test
    void parseInvalidHour() {
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("0 24 * * *"));
    }

    @Test
    void parseInvalidDayOfMonth() {
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("0 0 0 * *"));

        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("0 0 32 * *"));
    }

    @Test
    void parseInvalidMonth() {
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("0 0 * 0 *"));

        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("0 0 * 13 *"));
    }

    @Test
    void parseInvalidDayOfWeek() {
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("0 0 * * 8"));
    }

    @Test
    void parseInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> CronExpression.parse("30-10 * * * *"));
    }

    @Test
    void expressionReturnsOriginalString() {
        String expr = "0 9 * * 1-5";
        CronExpression parsed = CronExpression.parse(expr);
        assertEquals(expr, parsed.expression());
    }

    @Test
    void parseNullThrows() {
        assertThrows(NullPointerException.class, () -> CronExpression.parse(null));
    }
}
