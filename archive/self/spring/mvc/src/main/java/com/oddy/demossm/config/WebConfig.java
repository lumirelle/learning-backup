package com.oddy.demossm.config;

import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import com.oddy.demossm.entity.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

// 启用 Spring MVC 支持
@EnableWebMvc
@Configuration
@ComponentScan("com.oddy.demossm.controller")
// WebMvcConfigurer 是一个接口，可以用来配置 Spring MVC
// 静态资源需要通过 Tomcat 默认的 Servlet 处理，所以需要实现 WebMvcConfigurer 接口
// 重写 addResourceHandlers 方法，添加静态资源的处理
public class WebConfig implements WebMvcConfigurer {

  // 配置 Thymeleaf 视图解析器
  @Bean
  public ThymeleafViewResolver viewResolver(ISpringTemplateEngine templateEngine) {
    ThymeleafViewResolver resolver = new ThymeleafViewResolver();
    resolver.setOrder(1);
    resolver.setCharacterEncoding("UTF-8"); // 视图解析器使用的字符集，必须设置，否则中文乱码
    resolver.setTemplateEngine(templateEngine);
    return resolver;
  }

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

  // 5. Bean 的 Web 作用域
  @Bean
  // 请求作用域，每次请求都会创建一个新的实例
  @RequestScope
  @Scope("prototype")
  public User userRequest() {
    User user = new User();
    user.setUsername("Oddy Request");
    user.setPassword("123");
    return user;
  }

  @Bean
  // 会话作用域，每次会话都会创建一个新的实例
  @SessionScope
  @Scope("prototype")
  public User userSession() {
    User user = new User();
    user.setUsername("Oddy Session");
    user.setPassword("456");
    return user;
  }

  // 开启默认 Servlet 支持
  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  // 配置静态资源处理
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
  }

  // 配置拦截器
  // 多级拦截器会按一定顺序执行，
  // 默认先添加的先 preHandle，后添加的先 postHandle、afterCompletion
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 可以用 order 方法指定拦截器的执行顺序
//    registry.addInterceptor(new MainInterceptor()).addPathPatterns("/**");
//    registry.addInterceptor(new SubInterceptor()).addPathPatterns("/**");
  }

  // 配置消息转换器
  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    // FastJson 消息转换器，默认支持的 MediaType 为：*/*，
    // SpringMVC 出于安全考虑，不允许使用 */*，所以需要手动配置
    FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
    List<MediaType> mediaTypes = new ArrayList<>();
    mediaTypes.add(MediaType.APPLICATION_JSON);
    mediaTypes.add(MediaType.APPLICATION_XML);
    mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
    mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
    mediaTypes.add(MediaType.APPLICATION_ATOM_XML);
    mediaTypes.add(MediaType.APPLICATION_XHTML_XML);
    mediaTypes.add(MediaType.APPLICATION_PDF);
    mediaTypes.add(MediaType.APPLICATION_RSS_XML);
    mediaTypes.add(MediaType.APPLICATION_GRAPHQL_RESPONSE);
    mediaTypes.add(MediaType.APPLICATION_CBOR);
    mediaTypes.add(MediaType.APPLICATION_NDJSON);
    mediaTypes.add(MediaType.APPLICATION_PROBLEM_JSON);
    mediaTypes.add(MediaType.APPLICATION_PROBLEM_XML);
    mediaTypes.add(MediaType.APPLICATION_PROTOBUF);
    mediaTypes.add(MediaType.IMAGE_GIF);
    mediaTypes.add(MediaType.IMAGE_JPEG);
    mediaTypes.add(MediaType.IMAGE_PNG);
    mediaTypes.add(MediaType.MULTIPART_MIXED);
    mediaTypes.add(MediaType.MULTIPART_RELATED);
    mediaTypes.add(MediaType.MULTIPART_FORM_DATA);
    mediaTypes.add(MediaType.TEXT_PLAIN);
    mediaTypes.add(MediaType.TEXT_HTML);
    mediaTypes.add(MediaType.TEXT_XML);
    mediaTypes.add(MediaType.TEXT_MARKDOWN);
    mediaTypes.add(MediaType.TEXT_EVENT_STREAM);
    converter.setSupportedMediaTypes(mediaTypes);

    converters.add(converter);
  }

}
