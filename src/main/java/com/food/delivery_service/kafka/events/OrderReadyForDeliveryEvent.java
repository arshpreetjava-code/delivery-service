package com.food.delivery_service.kafka.events;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderReadyForDeliveryEvent {
    private String orderId;
    private String userId;
    private String address;
    private LocalDateTime readyTime;
    private String item;
    private String message;
}
