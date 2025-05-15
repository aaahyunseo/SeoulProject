package com.example.seoulproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String API_NAME = "예산트리 API";
    private static final String API_VERSION = "v1.0.0";
    private static final String API_DESCRIPTION = "2025 서울 공공데이터 창업 경진대회 예산트리 API 문서입니다.";

    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title(API_NAME)
                        .version(API_VERSION)
                        .description(API_DESCRIPTION)
                );
    }
}
