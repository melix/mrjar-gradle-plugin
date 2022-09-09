package demo.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FixedMessageTest {
    @Test void versionMessage() {
        String message = Version.getMessage();
        assertEquals("Java 11 version", message);
    }

    @Test void canCallVersionSpecificCode() {
        String message = Java11Specific.getMessage();
        assertEquals("Java 11 version", message);
    }
}
