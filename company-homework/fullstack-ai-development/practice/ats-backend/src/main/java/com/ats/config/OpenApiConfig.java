package com.ats.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI atsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ATS API")
                        .description("招聘管理系统 REST API")
                        .version("v1"));
    }
}
