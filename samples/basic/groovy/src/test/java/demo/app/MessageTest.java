package demo.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    @Test void versionMessage() {
        String message = Version.getMessage();
        assertEquals("Base version", message);
    }
}
