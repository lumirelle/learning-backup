package com.oddy.demospringboot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig.Builder;
import com.oddy.demospringboot.entity.Account;
import com.oddy.demospringboot.mapper.AccountPlusMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootTest
class DemoSpringBootApplicationTests {

  @Resource
  private PasswordEncoder passwordEncoder;

  @Test
  void passwordEncoderTest() {
    log.info("Password: " + passwordEncoder.encode("admin"));
  }

  @Resource
  private JdbcTemplate jdbcTemplate;

  @Resource
  private DataSource dataSource;

  @Test
  void springJDBCTest() {
    // Spring JDBC
    // 一条记录 Map
    Map<String, Object> map = jdbcTemplate.queryForMap(
        "select * from demo_spring_boot.account where id = 1");
    log.info(map.toString());

    // 多条记录 List
    List<Map<String, Object>> list = jdbcTemplate.queryForList(
        "select * from demo_spring_boot.account");
    log.info(list.toString());
    // 一条记录实体类
    Account account = jdbcTemplate.queryForObject(
        "select * from demo_spring_boot.account where id = 2", (rs, rowNum) -> {
          Account tmp = new Account();
          tmp.setId(rs.getInt(1));
          tmp.setName(rs.getString(2));
          tmp.setSex(rs.getString(3));
          tmp.setAge(rs.getInt(4));
          tmp.setEmail(rs.getString(5));
          tmp.setPassword(rs.getString(6));
          return tmp;
        });
    assert account != null;
    log.info(account.toString());

    // 使用参数查询
    int requestId = 1;
    map = jdbcTemplate.queryForMap("select * from demo_spring_boot.account where id = ?",
        requestId);
    log.info(map.toString());

    // Simple JDBC Insert，实现高级插入功能
    SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName(
            "account") // 指定插入数据的表名
        .usingGeneratedKeyColumns("id"); // 设置自增主键列
    map = new HashMap<>(2);
    map.put("name", "jdbc insert");
    map.put("email", "jdbc-insert@test.com");
    map.put("password", "$2a$10$D/Eyfj0KweoCsW8Jl6EM6.0ubDG4ZdCeIq3M9YuYSIQBZmuRIK8Oe"); // admin
    Number key = simpleJdbcInsert.executeAndReturnKey(map);
    log.info(key.toString());
  }

  @Resource
  private AccountPlusMapper accountPlusMapper;

  @Test
  void mybatisPlusTest() {
    // 自带的简单操作
    log.info(accountPlusMapper.selectById(1).toString());

    // 复杂操作，使用 QueryWrapper
    // 也可以使用工具类 Wrappers 创建 QueryWrapper
//    Wrappers.<Account>query()
//        .select("...");
    QueryWrapper<Account> wrapper = new QueryWrapper<>();
    wrapper.select("id", "name", "email", "password") // 筛选需要的列
        .ge("id", 2) // 指定某一列的值大于等于某值
        .orderByDesc("id"); // 根据某一列降序排序
    log.info(accountPlusMapper.selectList(wrapper).toString());

    // 分页操作，查询第一页，共分两页
    Page<Account> page = accountPlusMapper.selectPage(Page.of(1, 2), Wrappers.query());
    log.info(page.getRecords().toString());
  }

  @Test
  void mybatisPlusGenerate() {
    FastAutoGenerator.create(new Builder(dataSource))
        // 全局配置
        .globalConfig(builder -> builder.author("SeagullOddy (shabbyacc@outlook.com)")
            .commentDate("2023-09-03")
            .outputDir("/src/main/java"))
        // 打包配置
        .packageConfig(builder -> builder.parent("com.oddy"))
        // 策略配置，这里是为生成的 Mapper 添加 @Mapper 注解
        .strategyConfig(builder -> builder.mapperBuilder().mapperAnnotation(Mapper.class).build())
        // 模板配置，这里是用我们 /resource/template/ 目录下的模板 mapper.java.vm 来生成 Mapper 类
        .templateConfig(builder -> builder.mapper("/template/mapper.java.vm"))
        .execute();
  }

}
