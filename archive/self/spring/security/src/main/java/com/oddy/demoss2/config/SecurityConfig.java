package com.oddy.demoss2.config;

import javax.sql.DataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
// 开启 Web 安全支持
@EnableWebSecurity
public class SecurityConfig {

  // 密码加密器
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 数据源
  @Bean
  public DataSource dataSource() {
    return new PooledDataSource("com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/demo_ss2",
        "root", "oddy");
  }

  // Mybatis 中的 Mapper 需要 SqlSessionFactoryBean
  @Bean
  public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) {
    SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
    sqlSessionFactoryBean.setDataSource(dataSource);
    return sqlSessionFactoryBean;
  }

  // 1.2.2 基于数据库的认证
  // 提供一个认证管理器，用于处理密码校验？？
  // 为了在修改密码时，能够校验旧密码
//  private AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
//      PasswordEncoder passwordEncoder) {
//    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//    provider.setUserDetailsService(userDetailsService);
//    provider.setPasswordEncoder(passwordEncoder);
//    return new ProviderManager(provider);
//  }

  // 1. 认证——获取用户信息
  // 认证是根据用户提供的登录信息是否正确，决定是否允许登录（允许访问网页），
  // 获取用户信息的实现方式是向 Spring 容器中提供一个 UserDetailsService/UserDetailsManager 的实现类作为 bean
  // 提供之后，Spring 会在认证过程中自动使用该 bean 获取用户信息，然后将信息与用户输入进行对比
  // 1.1 使用已提供的实现（自定义程度低，不推荐）
//  @Bean
//  public UserDetailsService userDetailsService() {
  // 1.1.1 基于内存
  // 创建用来比对的用户信息并返回一个 InMemoryUserDetailsManager（每次都需要创建，不推荐）
//    UserDetails user = User.withDefaultPasswordEncoder() // 默认密码加密器（不推荐）
// // UserDetails user = User.withUsername("user")
//        .username("user")
//        .password("password") // 明文密码（不推荐）
// //     .password(passwordEncoder.encode("password")) // 自己提供一个密码加密器（推荐）
//        .roles("USER")
//        .build();
//    UserDetails admin = User.withDefaultPasswordEncoder() // 默认密码加密器（不推荐）
// // UserDetails admin = User.withUsername("admin")
//        .username("admin")
//        .password("password") // 明文密码（不推荐）
// //     .password(passwordEncoder.encode("password")) // 自己提供一个密码加密器（推荐）
//        .roles("ADMIN")
//        .build();
//    return new InMemoryUserDetailsManager(user, admin);
  // 1.1.2 基于数据库
  // 数据表结构是固定的，不够灵活，因此不推荐
  // 从数据库中读取用户信息，数据表结构规定如下：
  // CREATE TABLE `users` (
  //   `username` varchar(50) NOT NULL,
  //   `password` varchar(100) NOT NULL,
  //   `enabled` tinyint(1) NOT NULL,
  //   PRIMARY KEY (`username`)
  // ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  // CREATE TABLE `authorities` (
  //   `username` varchar(50) NOT NULL,
  //   `authority` varchar(50) NOT NULL,
  //   UNIQUE KEY `ix_auth_username` (`username`,`authority`),
  //   CONSTRAINT `fk_authorities_users` FOREIGN KEY (`username`) REFERENCES `users` (`username`)
  // ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
//    JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
//    manager.setAuthenticationManager(authenticationManager(manager, passwordEncoder));
  // createUser 方法用于在数据库中创建用户，也可以使用 SQL 脚本创建用户、手动创建用户等
//    manager.createUser(user);
//    manager.createUser(admin);
//    return manager;
//  }
  // 1.2 自己编写实现类获取用户信息，见 AccountUserDetailsService

  // 2.5.1 记住我中使用的持久化 Token 存储
  @Bean
  public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
    JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
    tokenRepository.setDataSource(dataSource);
    // 根据数据源自动创建存储表，第一次启动时需要，之后注释掉
//    tokenRepository.setCreateTableOnStartup(true);
    return tokenRepository;
  }

  // 2. 自定义过滤器链，和 5.0 版本不同
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
      PersistentTokenRepository tokenRepository) throws Exception {
    return httpSecurity
        // 2.1 配置页面拦截规则
        // 此处的授权配置应是粗配置，更细粒度的配置应在 Controller 中使用 @PreAuthorize 注解实现
        // 详见 MainController
        // Spring Security 6.1.2 版本会报此错误：
        // Servlet 3.0规范的第4.4节不允许从未在web.xml，web-fragment.xml文件中定义或未用@WebListener注释的ServletContextListener调用此方法。
        .authorizeHttpRequests(auth -> auth.requestMatchers("/static/**")
            .permitAll()
            .anyRequest()
            .authenticated())
        // 2.2 自定义表单登录
        .formLogin(login -> login.loginPage("/login")
            .loginProcessingUrl("/do-login")
            .defaultSuccessUrl("/")
            .permitAll())
        // 2.3 自定义退出登录
        .logout(logout -> logout.logoutUrl("/do-logout")
            .logoutSuccessUrl("/login")
            .permitAll())
        // 2.4 关闭 csrf，浏览器有足够的防护措施，不需要 csrf
        .csrf(AbstractHttpConfigurer::disable)
        // 2.5 记住我
        .rememberMe(remember -> {
          remember.tokenRepository(tokenRepository);
          remember.tokenValiditySeconds(7 * 24 * 3600);
        })
        // 构建
        .build();
  }

}
