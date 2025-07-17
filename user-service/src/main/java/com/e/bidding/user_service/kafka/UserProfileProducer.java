package com.e.bidding.user_service.kafka;

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
public class UserProfileProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileProducer.class);

    private final NewTopic profileCreationTopic;
    private final KafkaTemplate<String, ProfileCreatedResponseEventDTO> kafkaTemplate;

    public UserProfileProducer(NewTopic profileCreationTopic, KafkaTemplate<String, ProfileCreatedResponseEventDTO> kafkaTemplate) {
        this.profileCreationTopic = profileCreationTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(ProfileCreatedResponseEventDTO event){
        LOGGER.info(String.format("Order event => %s", event.toString()));

        //create Message
        Message<ProfileCreatedResponseEventDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC , profileCreationTopic.name())
                .build();

        System.out.println("send message to auth");
        kafkaTemplate.send(message);
    }
}
