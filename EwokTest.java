package bgu.spl.mics.application.passiveObjects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EwokTest {

    private Ewok E;

    @BeforeEach
    void setUp() {
        E = new Ewok(1);
    }

    @Test
    void testAcquire() {
        assertTrue(E.getAvailability());
        E.acquire();
        assertFalse(E.getAvailability());
    }

    @Test
    void testRelease() {
        E.acquire();
        assertFalse(E.getAvailability());
        E.release();
        assertTrue(E.getAvailability());
    }
}