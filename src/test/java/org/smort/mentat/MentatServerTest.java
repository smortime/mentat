package org.smort.mentat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MentatServerTest {
    private static MentatServer server;

    @BeforeAll
    static void setUp() {
        server = new MentatServer();
        server.start(4000);
    }

    @AfterAll
    static void shutdown() {
        server.stop();
    }

    @Test
    void testBindsCorrectPort() throws IOException, InterruptedException {
        // I know... gives time for server to startup
        TimeUnit.SECONDS.sleep(1);
        TestClient client = new TestClient();
        assertFalse(client.socketClosed());
    }

    @Test
    void testAcceptsValidInput() throws IOException, InterruptedException {
        // I know... gives time for server to startup
        TimeUnit.SECONDS.sleep(1);
        TestClient client = new TestClient();
        client.send("123456789");
        client.send("000000000");
        client.send("999999999");
        assertFalse(client.socketClosed());
    }

    @Test
    void testLogFileCreated() {
        File f = new File("numbers.log");
        assertTrue(f.exists());
        assertFalse(f.isDirectory());
    }
}
