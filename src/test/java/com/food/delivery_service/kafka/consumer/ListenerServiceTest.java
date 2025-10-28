package com.food.delivery_service.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.delivery_service.kafka.events.OrderItemPreparedEvent;
import com.food.delivery_service.kafka.events.OrderReadyForDeliveryEvent;
import com.food.delivery_service.kafka.events.PaymentEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static com.food.delivery_service.kafka.topics.KafkaTopics.ORDER_DELIVERED;
import static com.food.delivery_service.kafka.topics.KafkaTopics.ORDER_READY_FOR_DELIVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ListenerServiceTest {

    private ObjectMapper objectMapper;
    private KafkaTemplate<String, OrderReadyForDeliveryEvent> kafkaTemplate;
    private ListenerService listenerService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        listenerService = new ListenerService(objectMapper, kafkaTemplate);
    }

    @Test
    void onOrderPrepared_storesState_andAcknowledges_andTriesDeliver() throws Exception {
        OrderItemPreparedEvent prepared = OrderItemPreparedEvent.builder()
                .orderId("o1").userId("u1").itemType("pizza").quantity(1)
                .address("addr").completedTime(LocalDateTime.now())
                .build();
        String json = objectMapper.writeValueAsString(prepared);

        Acknowledgment ack = mock(Acknowledgment.class);

        listenerService.onOrderPrepared(json, ack);

        verify(ack, times(1)).acknowledge();
        // Not ready yet (no payment) -> no publish
        verify(kafkaTemplate, never()).send(eq(ORDER_READY_FOR_DELIVER), any(OrderReadyForDeliveryEvent.class));
    }

    @Test
    void onPaymentCompleted_marksPayment_andAcknowledges_andTriesDeliver() throws Exception {
        PaymentEvent payment = PaymentEvent.builder().orderId("o2").userId("u2").status("SUCCESS").amount(10.0).build();
        String json = objectMapper.writeValueAsString(payment);

        Acknowledgment ack = mock(Acknowledgment.class);

        listenerService.onPaymentCompleted(json, ack);

        verify(ack, times(1)).acknowledge();
        // Not ready yet (no prepared) -> no publish
        verify(kafkaTemplate, never()).send(eq(ORDER_READY_FOR_DELIVER), any(OrderReadyForDeliveryEvent.class));
    }

    @Test
    void tryDeliver_publishesReadyEvent_whenBothConditionsMet() throws JsonProcessingException {
        String orderId = "o3";

        // First send prepared
        OrderItemPreparedEvent prepared = OrderItemPreparedEvent.builder()
                .orderId(orderId).userId("u3").itemType("burger").quantity(2)
                .address("addr3").completedTime(LocalDateTime.now())
                .build();
        String preparedJson = objectMapper.writeValueAsString(prepared);

        // Then payment
        PaymentEvent payment = PaymentEvent.builder().orderId(orderId).userId("u3").status("SUCCESS").amount(20.0).build();
        String paymentJson = objectMapper.writeValueAsString(payment);

        Acknowledgment ack = mock(Acknowledgment.class);

        listenerService.onOrderPrepared(preparedJson, ack);
        listenerService.onPaymentCompleted(paymentJson, ack);

        ArgumentCaptor<OrderReadyForDeliveryEvent> captor = ArgumentCaptor.forClass(OrderReadyForDeliveryEvent.class);
        verify(kafkaTemplate, times(1)).send(eq(ORDER_READY_FOR_DELIVER), captor.capture());
        OrderReadyForDeliveryEvent sent = captor.getValue();
        assertEquals(orderId, sent.getOrderId());
        assertThat(sent.getMessage()).isEqualTo("Order is ready for delivery!");
    }

    @Test
    void deliverOrder_returnsNotReady_whenMissingState() throws Exception {
        // Create only payment state (no prepared event) so status exists but not ready
        String orderId = "oNotReady";
        PaymentEvent payment = PaymentEvent.builder().orderId(orderId).userId("uNR").status("SUCCESS").amount(5.0).build();
        String paymentJson = objectMapper.writeValueAsString(payment);
        Acknowledgment ack = mock(Acknowledgment.class);
        listenerService.onPaymentCompleted(paymentJson, ack);

        String result = listenerService.deliverOrder(orderId);
        assertThat(result).isEqualTo("Order not ready for delivery yet");
        verify(kafkaTemplate, never()).send(eq(ORDER_DELIVERED), any(OrderReadyForDeliveryEvent.class));
    }

    @Test
    void deliverOrder_publishesDelivered_andRemovesState_whenReady() throws JsonProcessingException {
        String orderId = "o4";

        // prepared
        OrderItemPreparedEvent prepared = OrderItemPreparedEvent.builder()
                .orderId(orderId).userId("u4").itemType("taco").quantity(3)
                .address("addr4").completedTime(LocalDateTime.now())
                .build();
        String preparedJson = objectMapper.writeValueAsString(prepared);

        // payment
        PaymentEvent payment = PaymentEvent.builder().orderId(orderId).userId("u4").status("SUCCESS").amount(30.0).build();
        String paymentJson = objectMapper.writeValueAsString(payment);

        Acknowledgment ack = mock(Acknowledgment.class);
        listenerService.onOrderPrepared(preparedJson, ack);
        listenerService.onPaymentCompleted(paymentJson, ack);

        reset(kafkaTemplate);

        String result = listenerService.deliverOrder(orderId);
        assertThat(result).isEqualTo("Order delivered for orderId " + orderId);

        ArgumentCaptor<OrderReadyForDeliveryEvent> captor = ArgumentCaptor.forClass(OrderReadyForDeliveryEvent.class);
        verify(kafkaTemplate, times(1)).send(eq(ORDER_DELIVERED), captor.capture());
        OrderReadyForDeliveryEvent delivered = captor.getValue();
        assertEquals(orderId, delivered.getOrderId());
        assertThat(delivered.getMessage()).isEqualTo("Order is delivered!");
    }
}


