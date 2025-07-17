package util;

public class DatabaseTypeUtil {

  private DatabaseTypeUtil() {
  }

  /**
   * 获取 MySQL 数据类型的最大值，如果不是数值类型则返回 -1
   *
   * @param mysqlType MySQL 数据类型
   * @return 最大值
   */
  public static long maxValueOf(String mysqlType) {
    switch (mysqlType) {
      case "tinyint" -> {
        return Byte.MAX_VALUE;
      }
      case "smallint" -> {
        return Short.MAX_VALUE;
      }
      case "int" -> {
        return Integer.MAX_VALUE;
      }
      case "bigint", "numeric", "decimal" -> {
        return Long.MAX_VALUE;
      }
      case "float" -> {
        return Float.MAX_EXPONENT;
      }
      case "double" -> {
        return Double.MAX_EXPONENT;
      }
      default -> {
        return -1;
      }
    }
  }

  /**
   * 获取 MySQL 数据类型的最小值，如果不是数值类型则返回 -2
   *
   * @param mysqlType MySQL 数据类型
   * @return 最小值
   */
  public static long minValueOf(String mysqlType) {
    switch (mysqlType) {
      case "tinyint" -> {
        return Byte.MIN_VALUE;
      }
      case "smallint" -> {
        return Short.MIN_VALUE;
      }
      case "int" -> {
        return Integer.MIN_VALUE;
      }
      case "bigint", "numeric", "decimal" -> {
        return Long.MIN_VALUE;
      }
      case "float" -> {
        return Float.MIN_EXPONENT;
      }
      case "double" -> {
        return Double.MIN_EXPONENT;
      }
      default -> {
        return -2;
      }
    }
  }
}
