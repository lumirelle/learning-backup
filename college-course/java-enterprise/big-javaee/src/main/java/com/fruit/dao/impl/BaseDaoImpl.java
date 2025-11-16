package com.fruit.dao.impl;

import com.fruit.dao.BaseDao;
import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;

@Getter
@Setter
public class BaseDaoImpl<T> extends SqlSessionDaoSupport implements BaseDao<T> {

  @Override
  @Resource
  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    super.setSqlSessionFactory(sqlSessionFactory);
  }

  private String ns;

  public List<T> find(Map<String, Object> map) {
    return this.getSqlSession().selectList(ns + ".find", map);
  }

  public T get(Serializable id) {
    return this.getSqlSession().selectOne(ns + ".get", id);
  }

  public void insert(T entity) {
    this.getSqlSession().insert(ns + ".insert", entity);
  }

  public void update(T entity) {
    this.getSqlSession().update(ns + ".update", entity);
  }

  public void deleteById(Serializable id) {
    this.getSqlSession().delete(ns + ".deleteById", id);
  }

  public void delete(Serializable[] ids) {
    this.getSqlSession().delete(ns + ".delete", ids);
  }

}
