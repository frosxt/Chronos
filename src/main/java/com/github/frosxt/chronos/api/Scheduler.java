package com.github.frosxt.chronos.api;

import com.github.frosxt.chronos.api.cron.CronExpression;

import java.time.Duration;
import java.time.ZoneId;

/**
 * A scheduler for executing tasks at specified times or intervals.
 *
 * <p>
 * Chronos provides multiple scheduling modes:
 * <ul>
 * <li>One-shot scheduling: execute once after a delay</li>
 * <li>Fixed-rate scheduling: execute at regular intervals from initial
 * time</li>
 * <li>Fixed-delay scheduling: execute with fixed delay between executions</li>
 * <li>Cron scheduling: execute according to a cron expression</li>
 * </ul>
 *
 * <p>
 * Implementations must be thread-safe.
 *
 * @see ScheduledHandle
 */
public interface Scheduler extends AutoCloseable {

    /**
     * Schedules a one-shot task to execute after the specified delay.
     *
     * @param delay the delay before execution, must be positive
     * @param task  the task to execute
     * @return a handle to control and monitor the scheduled task
     * @throws NullPointerException     if delay or task is null
     * @throws IllegalArgumentException if delay is not positive
     * @throws IllegalStateException    if the scheduler has been shut down
     */
    ScheduledHandle scheduleOnce(Duration delay, Runnable task);

    /**
     * Schedules a task to execute at a fixed rate.
     *
     * <p>
     * The next execution time is computed as
     * {@code initialScheduleTime + n * period}.
     * If an execution runs longer than the period, subsequent executions are
     * scheduled
     * to run immediately but will not overlap.
     *
     * @param initialDelay the delay before the first execution, must be
     *                     non-negative
     * @param period       the period between successive executions, must be
     *                     positive
     * @param task         the task to execute
     * @return a handle to control and monitor the scheduled task
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if initialDelay is negative or period is not
     *                                  positive
     * @throws IllegalStateException    if the scheduler has been shut down
     */
    ScheduledHandle scheduleAtFixedRate(Duration initialDelay, Duration period, Runnable task);

    /**
     * Schedules a task to execute with a fixed delay between executions.
     *
     * <p>
     * The next execution time is computed as
     * {@code endOfPreviousExecution + delay}.
     *
     * @param initialDelay the delay before the first execution, must be
     *                     non-negative
     * @param delay        the delay between the end of one execution and the start
     *                     of the next, must be positive
     * @param task         the task to execute
     * @return a handle to control and monitor the scheduled task
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if initialDelay is negative or delay is not
     *                                  positive
     * @throws IllegalStateException    if the scheduler has been shut down
     */
    ScheduledHandle scheduleWithFixedDelay(Duration initialDelay, Duration delay, Runnable task);

    /**
     * Schedules a task to execute according to a cron expression.
     *
     * <p>
     * Uses a default misfire grace period. If the scheduler falls behind,
     * missed executions are handled according to the misfire policy.
     *
     * @param cron the cron expression defining the schedule
     * @param zone the timezone for cron calculations
     * @param task the task to execute
     * @return a handle to control and monitor the scheduled task
     * @throws NullPointerException  if any argument is null
     * @throws IllegalStateException if the scheduler has been shut down
     */
    ScheduledHandle scheduleCron(CronExpression cron, ZoneId zone, Runnable task);

    /**
     * Schedules a task to execute according to a cron expression with a custom
     * misfire grace period.
     *
     * <p>
     * If the scheduler falls behind:
     * <ul>
     * <li>If within the misfire grace period: execute once immediately</li>
     * <li>Otherwise: skip to the next scheduled time</li>
     * </ul>
     *
     * @param cron         the cron expression defining the schedule
     * @param zone         the timezone for cron calculations
     * @param misfireGrace the grace period for handling misfires
     * @param task         the task to execute
     * @return a handle to control and monitor the scheduled task
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if misfireGrace is negative
     * @throws IllegalStateException    if the scheduler has been shut down
     */
    ScheduledHandle scheduleCron(CronExpression cron, ZoneId zone, Duration misfireGrace, Runnable task);

    /**
     * Returns a snapshot of the scheduler's current metrics.
     *
     * @return the scheduler snapshot, never null
     */
    SchedulerSnapshot snapshot();

    /**
     * Initiates an orderly shutdown.
     *
     * <p>
     * Previously submitted tasks are executed, but no new tasks will be accepted.
     * This method does not wait for previously submitted tasks to complete.
     */
    void shutdown();

    /**
     * Attempts to stop all actively executing tasks and halts the processing of
     * waiting tasks.
     *
     * <p>
     * This method does not wait for actively executing tasks to terminate.
     * Threads executing tasks are interrupted.
     */
    void shutdownNow();

    /**
     * Returns whether this scheduler has been shut down.
     *
     * @return {@code true} if shut down, {@code false} otherwise
     */
    boolean isShutdown();

    /**
     * Returns whether all tasks have completed following shutdown.
     *
     * @return {@code true} if terminated, {@code false} otherwise
     */
    boolean isTerminated();

    /**
     * Blocks until all tasks have completed after a shutdown request, or the
     * timeout occurs.
     *
     * @param timeout the maximum time to wait
     * @return {@code true} if terminated, {@code false} if the timeout elapsed
     * @throws NullPointerException     if timeout is null
     * @throws IllegalArgumentException if timeout is not positive
     * @throws InterruptedException     if interrupted while waiting
     */
    boolean awaitTermination(Duration timeout) throws InterruptedException;

    /**
     * Closes this scheduler by calling {@link #shutdown()}.
     */
    @Override
    void close();
}
