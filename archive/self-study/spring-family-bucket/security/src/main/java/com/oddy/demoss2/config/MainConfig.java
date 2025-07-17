package com.oddy.demoss2.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
@ComponentScan("com.oddy.demoss2.controller")
@ComponentScan("com.oddy.demoss2.service")
@MapperScan("com.oddy.demoss2.mapper")
// 开启 SpringMVC 支持（实际上就是导入了一些 SpringMVC 的配置类）
@EnableWebMvc
public class MainConfig implements WebMvcConfigurer {

  // 配置模板解析器
  @Bean
  public SpringResourceTemplateResolver templateResolver() {
    SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
    resolver.setPrefix("classpath:"); // 需要解析的文件位置，/ 是 webapp 目录下，classpath: 是类路径下
    resolver.setSuffix(".html"); // 需要解析的文件后缀
    resolver.setTemplateMode("HTML"); // HTML 模板
    resolver.setCharacterEncoding("UTF-8"); // 模板使用的字符集，必须设置，否则中文乱码
    return resolver;
  }

  // 配置模板引擎
  @Bean
  public SpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(templateResolver);
    return engine;
  }

  // 配置 Thymeleaf 视图解析器
  @Bean
  public ThymeleafViewResolver viewResolver(ISpringTemplateEngine templateEngine) {
    ThymeleafViewResolver resolver = new ThymeleafViewResolver();
    resolver.setOrder(1);
    resolver.setCharacterEncoding("UTF-8"); // 视图解析器使用的字符集，必须设置，否则中文乱码
    resolver.setTemplateEngine(templateEngine);
    return resolver;
  }

  // 启用默认的 Servlet
  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  // 配置静态资源的处理
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
  }

}
