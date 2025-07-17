package dao.impl;

import dao.BeanDAO;
import dao.connection.DBConnector;
import entity.Bean;
import entity.annotation.Column;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SimpleBeanDAO<T extends Bean> implements BeanDAO<T> {

  private final Class<T> beanClass;

  public SimpleBeanDAO(Class<T> beanClass) {
    this.beanClass = beanClass;
  }

  @Override
  public ArrayList<T> findBeans(String sql)
      throws SQLException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IntrospectionException {
    ArrayList<T> beans = new ArrayList<>();

    // 连接数据库，执行 SQL 语句，获取结果集，构造实体
    try (Connection connection = DBConnector.connect();
        ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
      Field[] fields = beanClass.getDeclaredFields();
      while (resultSet.next()) {
        T bean = beanClass.getConstructor().newInstance();
        constructBean(bean, resultSet, fields);
        beans.add(bean);
      }
    }

    return beans;
  }

  @Override
  public T findBeanByPrimaryKey(String sql, Object value)
      throws SQLException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IntrospectionException {
    T bean = null;

    // 连接数据库，执行 SQL 语句，获取结果集，构造实体
    try (Connection connection = DBConnector.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setObject(1, value);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        Field[] fields = beanClass.getDeclaredFields();
        while (resultSet.next()) {
          bean = beanClass.getConstructor().newInstance();
          constructBean(bean, resultSet, fields);
        }
      }
    }

    return bean;
  }

  @Override
  public int updateBean(String sql, Object... values)
      throws SQLException, IOException, ClassNotFoundException {
    // 更新实体（包括插入、修改、删除）只需要执行语句并返回受影响的行数
    try (Connection connection = DBConnector.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      int i = 1;
      for (Object value : values) {
        preparedStatement.setObject(i, value);
        i++;
      }
      return preparedStatement.executeUpdate();
    }
  }

  /**
   * 根据结果集构造实体
   *
   * @param bean      实体
   * @param resultSet 结果集
   * @param fields    实体的所有字段
   * @throws IntrospectionException    if an exception occurs during introspection.
   * @throws IllegalAccessException    if the caller does not have access to the property accessor
   *                                   method.
   * @throws InvocationTargetException if the property accessor method throws an exception.
   * @throws SQLException              if an exception occurs while accessing the database.
   */
  private void constructBean(T bean, ResultSet resultSet, Field[] fields)
      throws IntrospectionException, IllegalAccessException, InvocationTargetException, SQLException {
    for (Field field : fields) {
      PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), beanClass);
      Column column = field.getAnnotation(Column.class);
      if (column != null) {
        propertyDescriptor.getWriteMethod()
            .invoke(bean, resultSet.getObject(column.name(), field.getType()));
      }
    }
  }
}
