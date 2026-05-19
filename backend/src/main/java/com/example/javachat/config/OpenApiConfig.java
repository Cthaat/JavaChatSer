package com.example.javachat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI javaChatOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("JavaChatSer API")
                        .description("Spring Boot + Vue online chat system API.")
                        .version("0.1.0"));
    }
}
