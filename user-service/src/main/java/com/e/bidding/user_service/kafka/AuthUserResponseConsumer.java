package com.e.bidding.user_service.kafka;

import com.e.bidding.dtos.ProfileCreatedResponseEventDTO;
import com.e.bidding.user_service.service.UserProfileCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthUserResponseConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthUserResponseConsumer.class);

    private final UserProfileCreationService userProfileCreationService;

    public AuthUserResponseConsumer(UserProfileCreationService userProfileCreationService) {
        this.userProfileCreationService = userProfileCreationService;
    }

    @KafkaListener(topics = "auth_user_creation_response_topic", groupId = "auth_user_response")
    public void consume(ProfileCreatedResponseEventDTO event){

        LOGGER.info(String.format("User adding response from Authentication service => %s", event.toString()));

        try {
            if ("failure".equals(event.getStatus())) {
                userProfileCreationService.rollBackUserAdding(event.getUsername());
            } else {
                System.out.println("Registration is successful in both services");
            }
        } catch (Exception e) {
            throw new RuntimeException("Rollback failed!", e);
        }

        System.out.println(event);
    }
}