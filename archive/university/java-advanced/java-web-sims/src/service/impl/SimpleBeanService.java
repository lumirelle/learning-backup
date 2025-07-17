package service.impl;


import dao.impl.SimpleBeanDAO;
import entity.Bean;
import entity.annotation.Column;
import entity.annotation.Table;
import exception.InvalidValueException;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import service.BeanService;
import util.BeanClassAnalyzer;
import util.BeanDataValidator;

/**
 * 通用服务类，根据实体类的注解生成 SQL 语句，并执行 DAO 的对应方法，对数据库进行操作
 *
 * @param <T> 实体接口 Bean 的子类，表示数据库中的实体类
 */
public class SimpleBeanService<T extends Bean> implements BeanService<T> {

  private final Class<T> beanClass;

  private final SimpleBeanDAO<T> simpleDao;

  public SimpleBeanService(Class<T> beanClass) {
    this.beanClass = beanClass;
    this.simpleDao = new SimpleBeanDAO<>(beanClass);
  }

  @Override
  public ArrayList<T> findAllBeans() {
    try {
      // SELECT * FROM 表名
      Table beanTable = beanClass.getAnnotation(Table.class);
      return simpleDao.findBeans("SELECT * FROM %s".formatted(beanTable.name()));
    } catch (SQLException | IOException | ClassNotFoundException | NoSuchMethodException |
             InvocationTargetException | InstantiationException | IllegalAccessException |
             IntrospectionException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  @Override
  public T findBeanByPrimaryKey(Object value) {
    try {
      // SELECT * FROM 表名 WHERE 主码字段名 = '主码值'
      Table beanTable = beanClass.getAnnotation(Table.class);
      Column primaryKeyColumn = BeanClassAnalyzer.getPrimaryKeyField(beanClass)
          .getAnnotation(Column.class);
      BeanDataValidator.validateByColumn(value, primaryKeyColumn);
      return simpleDao.findBeanByPrimaryKey(
          "SELECT * FROM %s WHERE %s = ?".formatted(beanTable.name(), primaryKeyColumn.name()),
          value);
    } catch (SQLException | IntrospectionException | IOException | NoSuchFieldException |
             ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
             InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public int addBean(T bean) {
    try {
      Field[] fields = beanClass.getDeclaredFields();

      // INSERT INTO
      StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");

      // 表名 (
      Table beanTable = beanClass.getAnnotation(Table.class);
      sqlBuilder.append(beanTable.name()).append(" ( ");

      // 设置属性名，使得语句中属性的顺序和数据库中的可以不一致
      // 属性名, 属性名, ...
      for (Field field : fields) {
        Column column = field.getAnnotation(Column.class);
        sqlBuilder.append(column.name()).append(", ");
      }
      // 删除多余的逗号和空格
      sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

      // ) VALUES (
      sqlBuilder.append(" ) VALUES ( ");

      // ?, ?, ...
      Object[] values = new Object[fields.length];
      int i = 0;
      for (Field field : fields) {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), beanClass);
        Column column = field.getAnnotation(Column.class);
        // 获取值并进行校验
        values[i] = propertyDescriptor.getReadMethod().invoke(bean);
        BeanDataValidator.validateByColumn(values[i], column);
        i++;
        sqlBuilder.append("?, ");
      }
      sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

      // )
      sqlBuilder.append(")");

      return simpleDao.updateBean(sqlBuilder.toString(), values);
    } catch (SQLException | IntrospectionException | IOException | ClassNotFoundException |
             InvocationTargetException | IllegalAccessException | InvalidValueException e) {
      e.printStackTrace();
    }
    return -1;
  }

  @Override
  public int deleteBeanByPrimaryKey(Object value) {
    try {
      // DELETE FROM 表名 WHERE 主码字段名 = '主码值'
      Table beanTable = beanClass.getAnnotation(Table.class);
      Column primaryKeyColumn = BeanClassAnalyzer.getPrimaryKeyField(beanClass)
          .getAnnotation(Column.class);
      return simpleDao.updateBean(
          "DELETE FROM %s WHERE %s = ?".formatted(beanTable.name(), primaryKeyColumn.name()),
          value);
    } catch (NoSuchFieldException | SQLException | IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    return -1;
  }

  @Override
  public int updateBean(T bean) {
    try {
      // 获取主码字段和主码的 getter 方法
      Field primaryKeyField = BeanClassAnalyzer.getPrimaryKeyField(beanClass);
      PropertyDescriptor primaryKeyDescriptor = new PropertyDescriptor(primaryKeyField.getName(),
          beanClass);

      Field[] fields = Arrays.stream(beanClass.getDeclaredFields())
          .filter(field -> !field.equals(primaryKeyField)).toArray(Field[]::new);

      // UPDATE
      StringBuilder sqlBuilder = new StringBuilder("UPDATE ");

      // 表名 SET
      Table beanTable = beanClass.getAnnotation(Table.class);
      sqlBuilder.append(beanTable.name()).append(" SET ");

      // 字段名 = ?, 字段名 = ?, ...
      Object[] values = new Object[fields.length + 1];
      int i = 0;
      for (Field field : fields) {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), beanClass);
        Column fieldColumn = field.getAnnotation(Column.class);
        // 获取值并进行校验和处理
        values[i] = propertyDescriptor.getReadMethod().invoke(bean);
        BeanDataValidator.validateByColumn(values[i], fieldColumn);
        i++;
        sqlBuilder.append(fieldColumn.name()).append(" = ?, ");
      }
      sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

      // WHERE 主码字段名 = ?
      sqlBuilder.append(" WHERE ");
      values[i] = primaryKeyDescriptor.getReadMethod().invoke(bean);
      Column primaryKeyColumn = primaryKeyField.getAnnotation(Column.class);
      BeanDataValidator.validateByColumn(values[i], primaryKeyColumn);
      sqlBuilder.append(primaryKeyColumn.name()).append(" = ?");

      return simpleDao.updateBean(sqlBuilder.toString(), values);
    } catch (SQLException | IntrospectionException | IOException | ClassNotFoundException |
             InvocationTargetException | IllegalAccessException | InvalidValueException |
             NoSuchFieldException e) {
      e.printStackTrace();
    }
    return -1;
  }
}
