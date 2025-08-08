package com.e.bidding.user_service.kafka;

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
public class NewLocationProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewLocationProducer.class);
    private final NewTopic newLocationTopic;
    private final KafkaTemplate<String, LocationDTO> kafkaTemplate;

    public NewLocationProducer(NewTopic newLocationTopic, KafkaTemplate<String, LocationDTO> kafkaTemplate) {
        this.newLocationTopic = newLocationTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNewLocation(NewLocationWithYardManDTO event) {
        LOGGER.info(String.format("New Location event => %s", event.toString()));
        Message<NewLocationWithYardManDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, newLocationTopic.name())
                .build();
        kafkaTemplate.send(message);
    }
}
