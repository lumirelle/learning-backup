package com.oddy.demospringboot.repo;

import com.oddy.demospringboot.entity.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

// JPA 框架使用 Repository，需要继承 JpaRepository，其内置了一些基本的操作
// 两个泛型的类型分别是实体类类型和 ID 的类型
@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

  // 方法名拼接自定义 SQL 语句
  // JPA 通过方法名称的拼接，会自动为我们实现方法对应的功能，由此实现复杂操作
  // 如下方法 find accounts by name like... 意为 查找名称像……的多个账户
  List<Account> findAccountsByNameLike(String str);

  // JPQL 自定义 SQL 语句
  // DML 操作需要事务环境
  @Transactional
  // Modifying 表示是 DML 操作
  @Modifying
  // 这里使用 Account 类代替表模式，?n 表示方法的第 n 个参数
  @Query("update Account set password = ?2 where id = ?1")
  int updatePasswordById(Integer id, String password);

  // 原生 SQL 自定义 SQL 语句
  @Transactional
  @Modifying
  @Query(value = "update demo_spring_boot.account set password = :pwd where id = :id", nativeQuery = true)
  int updatePasswordByIdToo(@Param("id") Integer id, @Param("pwd") String password);

}
