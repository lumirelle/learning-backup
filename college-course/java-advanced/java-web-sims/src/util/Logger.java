package util;

import java.util.logging.Level;

public class Logger {

  private static final java.util.logging.Logger INNER_LOGGER = java.util.logging.Logger.getLogger(
      "SMS");

  private Logger() {
  }

  public static void info(String message) {
    INNER_LOGGER.log(Level.INFO, message);
  }

  public static void warning(String message) {
    INNER_LOGGER.log(Level.WARNING, message);
  }
}
