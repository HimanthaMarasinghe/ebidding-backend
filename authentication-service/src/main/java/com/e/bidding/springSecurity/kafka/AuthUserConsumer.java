package com.e.bidding.springSecurity.kafka;

import com.e.bidding.dtos.AuthUserCreationDTO;
import com.e.bidding.dtos.ProfileCreatedResponseEventDTO;
import com.e.bidding.springSecurity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthUserConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthUserConsumer.class);

    private final UserService userService;
    private final AuthUserResponseProducer authUserResponseProducer;

    public AuthUserConsumer(UserService userService, AuthUserResponseProducer authUserResponseProducer) {
        this.userService = userService;
        this.authUserResponseProducer = authUserResponseProducer;
    }

    @KafkaListener(topics = "auth_user_creation_topic", groupId = "adding_user_toAuth")
    public void consume(AuthUserCreationDTO event){

        LOGGER.info(String.format("User adding from user service => %s", event.toString()));
        ProfileCreatedResponseEventDTO profileCreatedResponseEventDTO = new ProfileCreatedResponseEventDTO();

        profileCreatedResponseEventDTO.setUsername(event.getUsername());
        try {
            userService.addUser(event);

            profileCreatedResponseEventDTO.setStatus("success");
            authUserResponseProducer.sendMessage(profileCreatedResponseEventDTO);
        } catch (Exception e) {

            profileCreatedResponseEventDTO.setStatus("failure");
            authUserResponseProducer.sendMessage(profileCreatedResponseEventDTO);

            throw new RuntimeException("Rollback failed!", e);
        }

        System.out.println(event);
    }
}
