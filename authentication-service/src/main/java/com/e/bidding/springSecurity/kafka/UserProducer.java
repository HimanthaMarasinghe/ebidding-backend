package com.e.bidding.springSecurity.kafka;

import com.e.bidding.dtos.ProfileCreationEventDTO;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class UserProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProducer.class);

    private final NewTopic userRegistrationTopic;
    private final KafkaTemplate<String, ProfileCreationEventDTO> kafkaTemplate;

    public UserProducer(NewTopic userRegistrationTopic, KafkaTemplate<String, ProfileCreationEventDTO> kafkaTemplate) {
        this.userRegistrationTopic = userRegistrationTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(ProfileCreationEventDTO event){
        LOGGER.info(String.format("Order event => %s", event.toString()));

        //create Message
        Message<ProfileCreationEventDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC , userRegistrationTopic.name())
                .build();

        kafkaTemplate.send(message);
    }
}
