package demo.app;

import org.apache.commons.lang3.StringUtils;
import lombok.Value;

@Value
public class Libs {

  private String greet;

  public static String sharedLib() {
    return StringUtils.EMPTY;
  }
}
