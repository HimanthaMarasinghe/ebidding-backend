package com.e.bidding.user_service.kafka;

import com.e.bidding.dtos.AuthUserCreationDTO;
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
public class AuthUserProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthUserProducer.class);

    private final NewTopic authUserCreationTopic;
    private final KafkaTemplate<String, AuthUserCreationDTO> kafkaTemplate;

    public AuthUserProducer(NewTopic authUserCreationTopic, KafkaTemplate<String, AuthUserCreationDTO> kafkaTemplate) {
        this.authUserCreationTopic = authUserCreationTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(AuthUserCreationDTO event){
        LOGGER.info(String.format("Order event => %s", event.toString()));

        //create Message
        Message<AuthUserCreationDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC , authUserCreationTopic.name())
                .build();

        kafkaTemplate.send(message);
    }
}
