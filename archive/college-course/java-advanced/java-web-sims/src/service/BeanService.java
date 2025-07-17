package service;

import java.util.ArrayList;

public interface BeanService<T> {

  /**
   * 查询所有实体
   *
   * @return 含所有实体的 ArrayList，若无实体则返回空 ArrayList
   */
  ArrayList<T> findAllBeans();

  /**
   * 根据主码查询实体。注意：只支持表中单主码的情况
   *
   * @param value 主码值
   * @return 主码符合条件的实体，若无符合条件的实体则返回 null
   */
  T findBeanByPrimaryKey(Object value);

  /**
   * 添加实体
   *
   * @param bean 实体
   * @return 受影响的行数，-1 表示操作失败
   */
  int addBean(T bean);

  /**
   * 根据主码（唯一）删除实体。注意：只支持单主码
   *
   * @param value 主码值
   * @return 受影响的行数，-1 表示操作失败
   */
  int deleteBeanByPrimaryKey(Object value);

  /**
   * 更新实体
   *
   * @param bean 实体
   * @return 受影响的行数，-1 表示操作失败
   */
  int updateBean(T bean);
}
