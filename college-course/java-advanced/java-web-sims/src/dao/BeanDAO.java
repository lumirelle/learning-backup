package dao;

import entity.Bean;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;

public interface BeanDAO<T extends Bean> {

  /**
   * 查询多个实体，无条件——返回所有实体；有条件——返回符合条件的实体
   *
   * @param sql SQL 语句
   * @return 含多个实体的 ArrayList, 若无符合条件的实体则返回空 ArrayList
   * @throws SQLException              操作失败
   * @throws IOException               读取配置文件失败
   * @throws ClassNotFoundException    加载类失败
   * @throws NoSuchMethodException     无法获取类的构造方法
   * @throws InvocationTargetException 无法调用类的构造方法
   * @throws InstantiationException    无法实例化类
   * @throws IllegalAccessException    无法访问类的构造方法
   * @throws IntrospectionException    无法获取类的属性
   */
  ArrayList<T> findBeans(String sql)
      throws SQLException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IntrospectionException;

  /**
   * 根据主码（唯一）查询单个实体。注意：只支持表中单主码的情况
   *
   * @param sql SQL 语句
   * @return 主码符合条件的实体，若无符合条件的实体则返回 null
   * @throws SQLException              操作失败
   * @throws IOException               读取配置文件失败
   * @throws ClassNotFoundException    加载类失败
   * @throws NoSuchMethodException     无法获取类的构造方法
   * @throws InvocationTargetException 无法调用类的构造方法
   * @throws InstantiationException    无法实例化类
   * @throws IllegalAccessException    无法访问类的构造方法
   * @throws IntrospectionException    无法获取类的属性
   */
  T findBeanByPrimaryKey(String sql, Object value)
      throws SQLException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IntrospectionException;


  /**
   * 更新实体，包括插入、修改、删除
   *
   * @param sql    SQL 语句
   * @param values SQL 语句中的参数
   * @return 受影响的行数
   * @throws SQLException           操作失败
   * @throws IOException            读取配置文件失败
   * @throws ClassNotFoundException 加载类失败
   */
  int updateBean(String sql, Object... values)
      throws SQLException, IOException, ClassNotFoundException;
}
