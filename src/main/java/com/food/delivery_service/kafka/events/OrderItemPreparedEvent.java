package com.food.delivery_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemPreparedEvent {
    private String userId;
    private String orderId;
    private String itemType;
    private int quantity;
    private String address;
    private LocalDateTime completedTime;
}
