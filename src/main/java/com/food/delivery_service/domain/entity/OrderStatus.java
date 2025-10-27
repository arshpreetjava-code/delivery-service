package com.food.delivery_service.domain.entity;

import com.food.delivery_service.kafka.events.OrderItemPreparedEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatus {
    private OrderItemPreparedEvent orderItemEvent;
    private boolean paymentDone = false;
}