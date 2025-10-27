package com.food.delivery_service.kafka.topics;

import lombok.Builder;

@Builder
public class KafkaTopics {
    public static final String ORDER_PREPARED = "order-prepared";
    public static final String PAYMENT_COMPLETED = "payment-completed";
    public static final String PAYMENT_FAILED = "payment-failed";
    public static final String ORDER_READY_FOR_DELIVER = "order-ready-for-delivery";
    public static final String ORDER_DELIVERED = "order-delivered";
}
