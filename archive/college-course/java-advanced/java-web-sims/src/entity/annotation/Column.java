package entity.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库的列（属性）注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

  String name();

  String type() default "varchar";

//  boolean increment() default false;

  boolean unsigned() default false;

//  boolean zerofill() default false;

  int length() default 255;

  int minLength() default 0;

//  int decimal() default -1;

  boolean notNull() default false;

  boolean primaryKey() default false;
}
