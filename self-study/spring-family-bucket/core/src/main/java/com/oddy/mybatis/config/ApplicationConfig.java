package com.oddy.mybatis.config;

import javax.sql.DataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan("com.oddy.mybatis.entity")
@ComponentScan("com.oddy.mybatis.service")
// 指定 mapper 方式 2
// 扫描指定包下的接口，注册为 Mapper，需要使用时直接从容器中取出即可
// 不需要从 SqlSessionFactory 中获取
@MapperScan("com.oddy.mybatis.mapper")
// 开启事务管理
@EnableTransactionManagement
public class ApplicationConfig {

  @Bean
  public TransactionManager transactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  // 不使用 xml 配置，使用 Java 配置，需要自己创建一个 DataSource
  @Bean
  public DataSource dataSource() {
    return new PooledDataSource("com.mysql.cj.jdbc.Driver",
        "jdbc:mysql://localhost:3306/demo_spring", "root", "oddy");
  }

  // 不使用 xml 配置
  @Bean
  public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource) {
    SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
    sqlSessionFactoryBean.setDataSource(dataSource);
    return sqlSessionFactoryBean;
  }

  // 使用 xml 配置文件
//  @Bean
//  public SqlSessionTemplate sqlSessionTemplate() throws IOException {
//    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
//        .build(Resources.getResourceAsReader("mybatis-config.xml"));
//    return new SqlSessionTemplate(sqlSessionFactory);
//  }

}
