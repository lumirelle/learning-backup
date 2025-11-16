package com.oddy.aop.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

// 注解：1. 启用 AOP 支持
@EnableAspectJAutoProxy
@ComponentScans(value = {
    @ComponentScan("com.oddy.aop.entity")
})
@Configuration
public class ApplicationConfig {

}
