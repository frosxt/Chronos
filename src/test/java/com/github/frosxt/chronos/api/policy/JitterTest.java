package com.github.frosxt.chronos.api.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Jitter}.
 */
class JitterTest {

    @Test
    void noneReturnsUnmodifiedDelay() {
        Jitter jitter = Jitter.none();

        assertEquals(0.0, jitter.factor());
        assertEquals(1000, jitter.apply(1000));
        assertEquals(0, jitter.apply(0));
    }

    @Test
    void uniformValidation() {
        assertThrows(IllegalArgumentException.class, () -> Jitter.uniform(-0.1));

        assertThrows(IllegalArgumentException.class, () -> Jitter.uniform(1.1));
    }

    @Test
    void uniformZeroReturnsNone() {
        Jitter jitter = Jitter.uniform(0.0);
        assertEquals(0.0, jitter.factor());
        assertEquals(1000, jitter.apply(1000));
    }

    @Test
    void uniformAppliesJitterWithinBounds() {
        Jitter jitter = Jitter.uniform(0.5);
        long baseDelay = 1_000_000_000L;

        for (int i = 0; i < 100; i++) {
            long jittered = jitter.apply(baseDelay);

            long minExpected = (long) (baseDelay * 0.5);
            long maxExpected = (long) (baseDelay * 1.5);

            assertTrue(jittered >= minExpected,
                    "Jittered delay " + jittered + " should be >= " + minExpected);
            assertTrue(jittered <= maxExpected,
                    "Jittered delay " + jittered + " should be <= " + maxExpected);
        }
    }

    @Test
    void uniformNeverReturnsNegative() {
        Jitter jitter = Jitter.uniform(1.0);

        for (int i = 0; i < 100; i++) {
            long jittered = jitter.apply(100);
            assertTrue(jittered >= 1, "Jittered delay should be at least 1");
        }
    }

    @Test
    void uniformPreservesZeroDelay() {
        Jitter jitter = Jitter.uniform(0.5);
        assertEquals(0, jitter.apply(0));
    }

    @Test
    void factorAccessor() {
        assertEquals(0.25, Jitter.uniform(0.25).factor());
        assertEquals(0.75, Jitter.uniform(0.75).factor());
    }

    @Test
    void toStringRepresentation() {
        assertEquals("Jitter[NONE]", Jitter.none().toString());
        assertTrue(Jitter.uniform(0.5).toString().contains("0.5"));
    }
}
