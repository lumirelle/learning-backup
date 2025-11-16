package com.oddy.demospringboot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info().title("Spring Boot Demo")
        .description("Api of spring boot demo.")
        .version("0.0.1")
        .license(new License().name("遵循 MIT 协议")
            .url("https://raw.githubusercontent.com/SeagullOddy/demo-spring-boot/main/LICENSE")));
  }

}
