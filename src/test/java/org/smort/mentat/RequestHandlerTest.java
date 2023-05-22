package org.smort.mentat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class RequestHandlerTest {
    @Test
    void testEnqueWork() {
        // Valid Input
        assertEquals(RequestHandler.parseInput("000000001"), 1);
        assertEquals(RequestHandler.parseInput("999999999"), 999999999);
        assertEquals(RequestHandler.parseInput("010101010"), 10101010);

        // Invalid Input
        assertThrows(NumberFormatException.class, () -> RequestHandler.parseInput("1234567890"));
        assertThrows(NumberFormatException.class, () -> RequestHandler.parseInput("0"));
        assertThrows(NumberFormatException.class, () -> RequestHandler.parseInput("aaaaaaaaa"));
        assertThrows(NumberFormatException.class, () -> RequestHandler.parseInput("12345678A"));
    }
}
