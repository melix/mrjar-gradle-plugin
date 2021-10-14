package demo.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OverridenTest {
    @Test void versionMessage() {
        String message = Version.getMessage();
        assertEquals("Java 11 version", message);
    }
}
