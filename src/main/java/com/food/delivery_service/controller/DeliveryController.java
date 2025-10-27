package com.food.delivery_service.controller;

import com.food.delivery_service.kafka.consumer.ListenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final ListenerService listenerService;

    /**
     * Trigger delivery for an order.
     * <p>
     * This endpoint attempts to mark the given order as delivered by delegating
     * to the ListenerService. If the order is not ready for delivery, the
     * service will return a message indicating that.
     *
     * @param orderId the id of the order to deliver (path variable)
     * @return ResponseEntity containing a human readable result message and HTTP 200
     */
    @PostMapping(path = "/{orderId}/deliver")
    public ResponseEntity<String> deliverOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(listenerService.deliverOrder(orderId));
    }

}
