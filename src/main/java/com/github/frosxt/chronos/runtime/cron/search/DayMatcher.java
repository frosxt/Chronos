package com.github.frosxt.chronos.runtime.cron.search;

import java.time.ZonedDateTime;

/**
 * Logic for matching day-of-month and day-of-week logic.
 */
public final class DayMatcher {

    private DayMatcher() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    public static boolean isValidDayOfMonth(final int day, final int[] daysOfMonth, final int[] daysOfWeek, final ZonedDateTime dt) {
        final boolean domRestricted = daysOfMonth.length < 31;
        final boolean dowRestricted = daysOfWeek.length < 7;

        final boolean domMatch = NextFieldSearch.isInArray(day, daysOfMonth);
        final int dow = dt.getDayOfWeek().getValue() % 7;
        final boolean dowMatch = NextFieldSearch.isInArray(dow, daysOfWeek);

        if (domRestricted && dowRestricted) {
            return domMatch || dowMatch;
        }
        return domMatch && dowMatch;
    }
}
