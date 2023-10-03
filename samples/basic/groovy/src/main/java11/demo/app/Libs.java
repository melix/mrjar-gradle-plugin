package demo.app;

import org.apache.commons.lang3.StringUtils;
import lombok.Value;

@Value
public class Libs {

  private String greet;

  public static String sharedLib() {
    final var empty = StringUtils.EMPTY; // to simulate Java 11 specific

    return empty;
  }
}
