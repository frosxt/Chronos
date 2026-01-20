package com.github.frosxt.chronos.runtime.trigger.impl;

import com.github.frosxt.chronos.api.cron.CronExpression;
import com.github.frosxt.chronos.runtime.time.mapper.InstantMapper;
import com.github.frosxt.chronos.runtime.trigger.Trigger;
import com.github.frosxt.chronos.runtime.trigger.cron.CronDelayCalculator;
import com.github.frosxt.chronos.runtime.trigger.cron.MisfirePolicy;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * A trigger for cron-based scheduling using composed logic.
 */
public final class CronTrigger implements Trigger {
    private final CronDelayCalculator calculator;
    private final MisfirePolicy misfirePolicy;
    private final InstantMapper instantMapper;
    private final ZoneId zone;

    public CronTrigger(final CronExpression cron, final ZoneId zone, final long misfireGraceNanos, final InstantMapper instantMapper) {
        this.calculator = new CronDelayCalculator(cron, zone);
        this.calculator.calculateNextFire(instantMapper.now());
        this.misfirePolicy = new MisfirePolicy(misfireGraceNanos);
        this.instantMapper = instantMapper;
        this.zone = zone;
    }

    @Override
    public long nextDelayNanos(final long currentNanos, final long lastStartNanos, final long lastEndNanos, final long runCount) {
        if (runCount > 0) {
            final ZonedDateTime reference = lastStartNanos >= 0 ? instantMapper.toInstant(lastStartNanos).atZone(zone) : instantMapper.now().atZone(zone);
            calculator.calculateNextFire(reference.toInstant());
        }

        if (calculator.nextFireTime() == null) {
            return -1;
        }

        final Instant now = instantMapper.now();
        Instant nextInstant = calculator.nextFireInstant();

        if (now.isAfter(nextInstant)) {
            final Duration elapsed = Duration.between(nextInstant, now);
            if (!misfirePolicy.isMisfire(elapsed)) {
                return 0;
            }

            calculator.calculateNextFire(now);
            if (calculator.nextFireTime() == null) {
                return -1;
            }
            nextInstant = calculator.nextFireInstant();
        }

        return Math.max(0, Duration.between(now, nextInstant).toNanos());
    }

    @Override
    public boolean isRecurring() {
        return true;
    }
}
