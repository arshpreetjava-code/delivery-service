package com.food.delivery_service.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deliveryServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Delivery Service API")
                        .description("APIs for managing delivery lifecycle")
                        .version("v0.0.1")
                        .license(new License().name("MIT")))
                .externalDocs(new ExternalDocumentation().description("Project docs"));
    }
}
