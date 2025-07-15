package com.e.bidding.springSecurity.kafka;

import com.e.bidding.dtos.ProfileCreatedResponseEventDTO;
import com.e.bidding.springSecurity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserProfileConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileConsumer.class);

    private final UserService userService;

    public UserProfileConsumer(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(topics = "profile_creation_topic", groupId = "user_profile_response")
    public void consume(ProfileCreatedResponseEventDTO event){

        LOGGER.info(String.format("User profile creation respone from user service => %s", event.toString()));

        try {
            if ("failure".equals(event.getStatus())) {
                userService.rollbackRegistration(event.getUsername());
            } else {
                System.out.println("Registration is successful in both services");
            }
        } catch (Exception e) {
            throw new RuntimeException("Rollback failed!", e);
        }

        System.out.println(event);
    }
}
