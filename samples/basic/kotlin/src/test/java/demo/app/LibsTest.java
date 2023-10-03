package demo.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LibsTest {

  @Test void sharedLib() {
    Libs libs = new Libs("Hello world!");

    assertEquals(libs.sharedLib(), "");
  }

  @Test void getGreet() {
    Libs libs = new Libs("Hello world!");

    assertEquals(libs.getGreet(), "Hello world!");
  }
}
