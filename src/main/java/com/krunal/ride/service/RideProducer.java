package com.krunal.ride.service;

import com.krunal.ride.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideProducer {

    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;
    private static final String TOPIC = "ride-requests";

    public void sendRideRequest(RideRequestedEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}
