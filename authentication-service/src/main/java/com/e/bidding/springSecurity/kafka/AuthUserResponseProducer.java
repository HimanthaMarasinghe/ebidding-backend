package com.e.bidding.springSecurity.kafka;

import com.e.bidding.dtos.ProfileCreatedResponseEventDTO;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class AuthUserResponseProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthUserResponseProducer.class);

    private final NewTopic authUserCreationResponseTopic;
    private final KafkaTemplate<String, ProfileCreatedResponseEventDTO> kafkaTemplate;

    public AuthUserResponseProducer(NewTopic authUserCreationResponseTopic, KafkaTemplate<String, ProfileCreatedResponseEventDTO> kafkaTemplate) {
        this.authUserCreationResponseTopic = authUserCreationResponseTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(ProfileCreatedResponseEventDTO event){
        LOGGER.info(String.format("Order event => %s", event.toString()));

        //create Message
        Message<ProfileCreatedResponseEventDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC , authUserCreationResponseTopic.name())
                .build();

        System.out.println("send message to auth");
        kafkaTemplate.send(message);
    }
}