package com.food.delivery_service.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.delivery_service.domain.entity.OrderStatus;
import com.food.delivery_service.kafka.events.OrderItemPreparedEvent;
import com.food.delivery_service.kafka.events.OrderReadyForDeliveryEvent;
import com.food.delivery_service.kafka.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.food.delivery_service.kafka.topics.KafkaTopics.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListenerService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, OrderReadyForDeliveryEvent> kafkaTemplate;

    private final Map<String, OrderStatus> orderStatusMap =  new ConcurrentHashMap<>();

    /**
     * Handle ORDER_PREPARED events by storing the prepared item details and
     * attempting delivery if payment is already done.
     *
     * @param message the raw JSON message from Kafka
     * @param ack     the Kafka acknowledgment to be called after processing
     * @throws JsonProcessingException if the message cannot be deserialized
     */
    @KafkaListener(topics = ORDER_PREPARED, groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderPrepared(String message, Acknowledgment ack) throws JsonProcessingException {
        OrderItemPreparedEvent orderItemEvent = objectMapper.readValue(message, OrderItemPreparedEvent.class);
        String orderId = orderItemEvent.getOrderId();

        OrderStatus status = orderStatusMap.computeIfAbsent(orderId, id -> new OrderStatus());
        status.setOrderItemEvent(orderItemEvent);
        log.info("Order Prepared received for {}", orderId);

        ack.acknowledge();

        tryDeliver(orderId);
    }

    /**
     * Handle PAYMENT_COMPLETED events by marking payment as done and attempting
     * delivery if the order is prepared.
     *
     * @param message the raw JSON message from Kafka
     * @param ack     the Kafka acknowledgment to be called after processing
     * @throws JsonProcessingException if the message cannot be deserialized
     */
    @KafkaListener(topics = PAYMENT_COMPLETED, groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentCompleted(String message, Acknowledgment ack) throws JsonProcessingException {
        PaymentEvent paymentEvent = objectMapper.readValue(message, PaymentEvent.class);
        String orderId = paymentEvent.getOrderId();

        OrderStatus status = orderStatusMap.computeIfAbsent(orderId, id -> new OrderStatus());
        status.setPaymentDone(true);
        log.info("Payment completed for {}", orderId);

        ack.acknowledge();

        tryDeliver(orderId);
    }

    /**
     * Attempt to deliver an order if both payment is done and the order item is prepared.
     * <p>
     * If both conditions are satisfied this method will publish an
     * ORDER_READY_FOR_DELIVER event containing the ready-for-delivery details.
     *
     * @param orderId the id of the order to check for delivery readiness
     */
    private void tryDeliver(String orderId) {
        OrderStatus status = orderStatusMap.get(orderId);
        if (status != null && status.isPaymentDone() && status.getOrderItemEvent() != null) {
            log.info("Delivering order {} (both ready + paid)", orderId);

            OrderReadyForDeliveryEvent orderReadyEvent = OrderReadyForDeliveryEvent.builder()
                    .orderId(orderId)
                    .userId(status.getOrderItemEvent().getUserId())
                    .address(status.getOrderItemEvent().getAddress())
                    .item(status.getOrderItemEvent().getItemType())
                    .readyTime(status.getOrderItemEvent().getCompletedTime())
                    .message("Order is ready for delivery!")
                    .build();

            kafkaTemplate.send(ORDER_READY_FOR_DELIVER, orderReadyEvent);
        }
    }

    /**
     * Deliver an order immediately via manual trigger.
     * <p>
     * This method is called by the delivery controller to perform a manual
     * delivery operation. If the order is not ready (payment missing or item not
     * prepared) a descriptive message is returned. On success it publishes an
     * ORDER_DELIVERED event, removes the order from internal state and returns
     * a success message.
     *
     * @param orderId the id of the order to deliver
     * @return a human readable message indicating success or reason for failure
     */
    public String deliverOrder(String orderId) {
        OrderStatus status = orderStatusMap.get(orderId);

        if(!status.isPaymentDone() || status.getOrderItemEvent() == null) {
            return "Order not ready for delivery yet";
        }

        OrderReadyForDeliveryEvent orderReadyEvent = OrderReadyForDeliveryEvent.builder()
                .orderId(orderId)
                .userId(status.getOrderItemEvent().getUserId())
                .address(status.getOrderItemEvent().getAddress())
                .item(status.getOrderItemEvent().getItemType())
                .readyTime(status.getOrderItemEvent().getCompletedTime())
                .message("Order is delivered!")
                .build();

        kafkaTemplate.send(ORDER_DELIVERED, orderReadyEvent);
        orderStatusMap.remove(orderId);
        return "Order delivered for orderId " + orderId;
    }
}
