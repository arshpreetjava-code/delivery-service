package com.food.delivery_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=localhost:0",
        "spring.kafka.consumer.group-id=test-group"
})
class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void openApiBean_isPresent_andHasInfo() {
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Delivery Service API");
    }
}


