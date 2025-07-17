package util;

import entity.Bean;
import entity.annotation.Column;
import java.lang.reflect.Field;

public class BeanClassAnalyzer {

  private BeanClassAnalyzer() {
  }

  public static Field getPrimaryKeyField(Class<? extends Bean> beanClass)
      throws NoSuchFieldException {
    for (Field field : beanClass.getDeclaredFields()) {
      Column column = field.getAnnotation(Column.class);
      if (column != null && column.primaryKey()) {
        return field;
      }
    }
    throw new NoSuchFieldException("BeanAnalyzer.getPrimaryField(): 未找到主键");
  }
}
