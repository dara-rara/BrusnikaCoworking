package com.example.BrusnikaCoworking.config.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void produce(String topic, T t) {
        kafkaTemplate.send(
                topic, t
        ).whenComplete((res, th) -> {
            log.info("produced message: " + res.getProducerRecord() + " topic: " + res.getProducerRecord().topic());
        });
    }
}
