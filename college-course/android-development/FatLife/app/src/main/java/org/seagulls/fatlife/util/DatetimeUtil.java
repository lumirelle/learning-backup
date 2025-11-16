package org.seagulls.fatlife.util;

import android.text.format.DateFormat;
import java.util.Date;

public abstract class DatetimeUtil {

  private DatetimeUtil() {
  }

  public static String nowFormatted() {
    return DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date())
        .toString();
  }

}
