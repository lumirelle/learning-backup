package com.oddy.el.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("com.oddy.el.entity")
// 加载外部配置文件，其中 encoding 属性用于指定配置文件的编码格式，如果不指定，默认为 ISO-8859-1，中文会出现乱码
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class ApplicationConfig {

}
