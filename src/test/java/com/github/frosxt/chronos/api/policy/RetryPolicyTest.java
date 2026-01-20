package com.github.frosxt.chronos.api.policy;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RetryPolicy}.
 */
class RetryPolicyTest {

    @Test
    void fixedDelayValidation() {
        assertThrows(NullPointerException.class, () -> RetryPolicy.fixedDelay(null, 3));

        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.fixedDelay(Duration.ZERO, 3));

        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.fixedDelay(Duration.ofSeconds(-1), 3));

        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.fixedDelay(Duration.ofSeconds(1), 0));
    }

    @Test
    void fixedDelayReturnsConstantDelay() {
        RetryPolicy policy = RetryPolicy.fixedDelay(Duration.ofSeconds(5), 3);

        assertEquals(RetryPolicy.Type.FIXED_DELAY, policy.type());
        assertEquals(3, policy.maxAttempts());

        long expectedNanos = Duration.ofSeconds(5).toNanos();
        assertEquals(expectedNanos, policy.delayNanosForAttempt(1));
        assertEquals(expectedNanos, policy.delayNanosForAttempt(2));
        assertEquals(expectedNanos, policy.delayNanosForAttempt(3));
    }

    @Test
    void exponentialBackoffValidation() {
        assertThrows(NullPointerException.class,
                () -> RetryPolicy.exponentialBackoff(null, Duration.ofMinutes(1), 2.0, 3));

        assertThrows(NullPointerException.class,
                () -> RetryPolicy.exponentialBackoff(Duration.ofSeconds(1), null, 2.0, 3));

        assertThrows(IllegalArgumentException.class,
                () -> RetryPolicy.exponentialBackoff(Duration.ZERO, Duration.ofMinutes(1), 2.0, 3));

        assertThrows(IllegalArgumentException.class,
                () -> RetryPolicy.exponentialBackoff(Duration.ofSeconds(1), Duration.ofMillis(500), 2.0, 3));

        assertThrows(IllegalArgumentException.class,
                () -> RetryPolicy.exponentialBackoff(Duration.ofSeconds(1), Duration.ofMinutes(1), 0.5, 3));

        assertThrows(IllegalArgumentException.class,
                () -> RetryPolicy.exponentialBackoff(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, 0));
    }

    @Test
    void exponentialBackoffCalculation() {
        RetryPolicy policy = RetryPolicy.exponentialBackoff(
                Duration.ofSeconds(1),
                Duration.ofSeconds(30),
                2.0,
                5);

        assertEquals(RetryPolicy.Type.EXPONENTIAL_BACKOFF, policy.type());
        assertEquals(5, policy.maxAttempts());

        long oneSecNanos = Duration.ofSeconds(1).toNanos();
        assertEquals(oneSecNanos, policy.delayNanosForAttempt(1));
        assertEquals(oneSecNanos * 2, policy.delayNanosForAttempt(2));
        assertEquals(oneSecNanos * 4, policy.delayNanosForAttempt(3));
        assertEquals(oneSecNanos * 8, policy.delayNanosForAttempt(4));
        assertEquals(oneSecNanos * 16, policy.delayNanosForAttempt(5));
    }

    @Test
    void exponentialBackoffCapsAtMax() {
        RetryPolicy policy = RetryPolicy.exponentialBackoff(
                Duration.ofSeconds(1),
                Duration.ofSeconds(5),
                2.0,
                10);

        long maxNanos = Duration.ofSeconds(5).toNanos();
        assertEquals(maxNanos, policy.delayNanosForAttempt(5));
        assertEquals(maxNanos, policy.delayNanosForAttempt(10));
    }

    @Test
    void delayNanosForAttemptValidation() {
        RetryPolicy policy = RetryPolicy.fixedDelay(Duration.ofSeconds(1), 3);

        assertThrows(IllegalArgumentException.class, () -> policy.delayNanosForAttempt(0));

        assertThrows(IllegalArgumentException.class, () -> policy.delayNanosForAttempt(-1));
    }
}
