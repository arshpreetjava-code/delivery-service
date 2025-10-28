package com.food.delivery_service.controller;

import com.food.delivery_service.kafka.consumer.ListenerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeliveryController.class)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListenerService listenerService;

    @Test
    void deliverOrder_returnsServiceMessage() throws Exception {
        when(listenerService.deliverOrder("123")).thenReturn("Order delivered for orderId 123");

        mockMvc.perform(post("/api/delivery/123/deliver"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order delivered for orderId 123"));
    }
}


