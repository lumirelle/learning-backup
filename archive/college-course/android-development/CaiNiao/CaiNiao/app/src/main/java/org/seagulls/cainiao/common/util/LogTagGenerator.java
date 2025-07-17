package org.seagulls.cainiao.common.util;

public class LogTagGenerator {

  private LogTagGenerator() {
  }

  public static String generate(Class<?> aClass) {
    String className = aClass.getName();
    String[] parts = className.split("\\.");
    StringBuilder tag = new StringBuilder();
    for (int i = 0; i < parts.length - 1; i++) {
      tag.append(parts[i].charAt(0));
      tag.append('.');
    }
    tag.append(parts[parts.length - 1]);
    return tag.toString();
  }

}
