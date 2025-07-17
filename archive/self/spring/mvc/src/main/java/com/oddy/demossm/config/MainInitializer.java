package com.oddy.demossm.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration.Dynamic;
import java.io.File;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

// 有了 SpringMVC 之后，不需要一个请求一个 Servlet 了，
// 它使用 DispatcherServlet 替代 Tomcat 提供的默认的静态 Servlet，
// 现在除了 jsp，所有的请求都由 DispatcherServlet 处理。
// Initializer 相当于 web.xml 的 Java 版
public class MainInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  /**
   * 定义基本的配置类，一般用于业务层
   *
   * @return 配置类
   */
  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class[]{WebConfig.class};
  }

  /**
   * 配置 DispatcherServlet 的配置类，一般用于 Controller 层等
   *
   * @return 配置类
   */
  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[0];
  }

  @Override
  protected String[] getServletMappings() {
    return new String[]{"/"};
  }

  /**
   * 通过 Registration 配置 Mutipart 相关配置，实现文件上传
   *
   * @param registration the {@code DispatcherServlet} registration to be customized
   */
  @Override
  protected void customizeRegistration(Dynamic registration) {
    File file = new File("C:/Users/Oddy/Downloads/uploads/");
    if (!file.exists()) {
      file.mkdirs();
    }
    registration.setMultipartConfig(new MultipartConfigElement(file.getAbsolutePath()));
  }

}
