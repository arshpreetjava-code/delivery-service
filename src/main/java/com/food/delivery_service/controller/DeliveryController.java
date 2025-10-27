package com.food.delivery_service.controller;

import com.food.delivery_service.kafka.consumer.ListenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final ListenerService listenerService;

    @PostMapping(path = "/{orderId}/deliver")
    public ResponseEntity<String> deliverOrder(String orderId) {
        return ResponseEntity.ok(listenerService.deliverOrder(orderId));
    }

}
