package com.github.frosxt.chronos.runtime.trigger.cron;

import com.github.frosxt.chronos.api.cron.CronExpression;
import com.github.frosxt.chronos.runtime.cron.CronNextFireCalculator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Calculates execution delays for cron triggers.
 */
public final class CronDelayCalculator {
    private final CronExpression cron;
    private final ZoneId zone;

    private volatile ZonedDateTime nextFireTime;

    public CronDelayCalculator(final CronExpression cron, final ZoneId zone) {
        this.cron = cron;
        this.zone = zone;
    }

    public void calculateNextFire(final Instant reference) {
        final ZonedDateTime zdt = reference.atZone(zone);
        this.nextFireTime = CronNextFireCalculator.nextFire(cron, zone, zdt);
    }

    public ZonedDateTime nextFireTime() {
        return nextFireTime;
    }

    public Instant nextFireInstant() {
        return nextFireTime != null ? nextFireTime.toInstant() : null;
    }
}
