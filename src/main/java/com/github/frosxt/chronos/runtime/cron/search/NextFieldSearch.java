package com.github.frosxt.chronos.runtime.cron.search;

/**
 * Helper for finding the next matching value in a sorted array.
 */
public final class NextFieldSearch {

    private NextFieldSearch() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    public static int findNext(final int current, final int[] arr) {
        for (final int v : arr) {
            if (v >= current) {
                return v;
            }
        }
        return -1;
    }

    public static boolean isInArray(final int value, final int[] arr) {
        for (final int v : arr) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }
}
