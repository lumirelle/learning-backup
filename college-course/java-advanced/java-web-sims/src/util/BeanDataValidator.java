package util;

import entity.annotation.Column;
import exception.InvalidValueException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class BeanDataValidator {

  private BeanDataValidator() {
  }

  public static void validateByColumn(Object value, Column column) throws InvalidValueException {
    // 空值需要先检验
    if ((column.notNull() || column.primaryKey()) && value == null) {
      throw new InvalidValueException("主键或不为空的属性，值不能为空");
    }
    // 值与类型匹配检验
    else if (!validateType(value, column.type())) {
      throw new InvalidValueException(
          "值与类型不匹配：" + value.getClass().getSimpleName() + " 与 " + column.type());
    }
    // 数值类型检验
    else if (value instanceof Number numberValue) {
      validateNumberByColumn(numberValue, column);
    }
    // 字符串类型检验
    else if (value instanceof String stringValue) {
      validateStringByColumn(stringValue, column);
    }
  }

  public static void validateNumberByColumn(Number number, Column column) {
    // 无符号数检验
    if (column.unsigned() && number.doubleValue() < 0) {
      throw new InvalidValueException("无符号数不能为负");
    }
    // 数值大小检验
    else if (number.doubleValue() > DatabaseTypeUtil.maxValueOf(column.type())
        || number.doubleValue() < DatabaseTypeUtil.minValueOf(column.type())) {
      throw new InvalidValueException("数值超出限制");
    }
    // increment、zerofill、decimal 暂不检验
  }

  public static void validateStringByColumn(String stringValue, Column column) {
    // 字符串长度检验
    if (stringValue.length() > column.length() || stringValue.length() < column.minLength()) {
      throw new InvalidValueException("字符串长度不符合限制");
    }
  }

  public static boolean validateType(Object value, String mysqlType) {
    if (value == null) {
      return false;
    }

    // 以下类型常用
    if (value instanceof String stringValue) {
      return validateStringType(stringValue, mysqlType);
    } else if (value instanceof Boolean booleanValue) {
      return validateBooleanType(booleanValue, mysqlType);
    } else if (value instanceof Integer) {
      return mysqlType.equals("int");
    } else if (value instanceof Date) {
      return mysqlType.equals("date");
    } else if (value instanceof Time) {
      return mysqlType.equals("time");
    }

    // 以下类型不常用
    else if (value instanceof Byte) {
      return mysqlType.equals("tinyint");
    } else if (value instanceof Short) {
      return mysqlType.equals("smallint");
    } else if (value instanceof Long) {
      return mysqlType.equals("bigint");
    } else if (value instanceof Float) {
      return mysqlType.equals("float");
    } else if (value instanceof Double) {
      return mysqlType.equals("double");
    } else if (value instanceof BigDecimal) {
      return mysqlType.equals("numeric") || mysqlType.equals("decimal");
    } else if (value instanceof Timestamp) {
      return mysqlType.equals("datetime") || mysqlType.equals("timestamp");
    }

    return false;
  }

  public static boolean validateStringType(String stringValue, String mysqlType) {
    if (stringValue == null) {
      return false;
    }
    return mysqlType.equals("char") || mysqlType.equals("varchar") || mysqlType.equals("text");
  }

  public static boolean validateBooleanType(Boolean booleanValue, String mysqlType) {
    if (booleanValue == null) {
      return false;
    }
    return mysqlType.equals("tinyint") || mysqlType.equals("bool") || mysqlType.equals("boolean");
  }
}
