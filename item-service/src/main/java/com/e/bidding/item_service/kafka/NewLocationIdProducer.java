package com.e.bidding.item_service.kafka;

import com.e.bidding.dtos.LocationDTO;
import com.e.bidding.dtos.NewLocationWithYardManDTO;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class NewLocationIdProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewLocationIdProducer.class);
    private final NewTopic newLocationIdTopic;
    private final KafkaTemplate<String, NewLocationWithYardManDTO> kafkaTemplate;

    public NewLocationIdProducer(NewTopic newLocationIdTopic, KafkaTemplate<String, NewLocationWithYardManDTO> kafkaTemplate) {
        this.newLocationIdTopic = newLocationIdTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNewLocationId(NewLocationWithYardManDTO event) {
        LOGGER.info(String.format("New Location event => %s", event.toString()));

        Message<NewLocationWithYardManDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC , newLocationIdTopic.name())
                .build();
        kafkaTemplate.send(message);
    }
}
