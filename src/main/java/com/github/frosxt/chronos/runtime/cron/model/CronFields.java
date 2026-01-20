package com.github.frosxt.chronos.runtime.cron.model;

import com.github.frosxt.chronos.api.cron.CronExpression;

/**
 * Holds the parsed and sorted fields of a cron expression.
 */
public final class CronFields {
    private final int[] minutes;
    private final int[] hours;
    private final int[] daysOfMonth;
    private final int[] months;
    private final int[] daysOfWeek;

    public CronFields(final CronExpression cron) {
        this.minutes = cron.minutes();
        this.hours = cron.hours();
        this.daysOfMonth = cron.daysOfMonth();
        this.months = cron.months();
        this.daysOfWeek = cron.daysOfWeek();
    }

    public CronFields(final int[] minutes, final int[] hours, final int[] daysOfMonth, final int[] months, final int[] daysOfWeek) {
        this.minutes = minutes;
        this.hours = hours;
        this.daysOfMonth = daysOfMonth;
        this.months = months;
        this.daysOfWeek = daysOfWeek;
    }

    public int[] minutes() {
        return minutes;
    }

    public int[] hours() {
        return hours;
    }

    public int[] daysOfMonth() {
        return daysOfMonth;
    }

    public int[] months() {
        return months;
    }

    public int[] daysOfWeek() {
        return daysOfWeek;
    }
}
