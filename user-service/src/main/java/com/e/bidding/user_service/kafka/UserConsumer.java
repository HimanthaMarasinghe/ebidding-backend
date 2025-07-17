package com.e.bidding.user_service.kafka;

import com.e.bidding.dtos.ProfileCreationEventDTO;
import com.e.bidding.dtos.ProfileCreatedResponseEventDTO;
import com.e.bidding.user_service.service.UserProfileCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserConsumer.class);

    private final UserProfileProducer userProfileProducer;

    private final UserProfileCreationService userProfileCreationService;

    public UserConsumer(UserProfileProducer userProfileProducer, UserProfileCreationService userProfileCreationService) {
        this.userProfileProducer = userProfileProducer;
        this.userProfileCreationService = userProfileCreationService;
    }

    @KafkaListener(topics = "user_registration_topic", groupId = "user_profile")
    public void consume(ProfileCreationEventDTO event){

        LOGGER.info(String.format("User registration event received in profile manager service => %s", event.toString()));
        ProfileCreatedResponseEventDTO profileCreatedResponseEventDTO = new ProfileCreatedResponseEventDTO();

        profileCreatedResponseEventDTO.setUsername(event.getUsername());

        try{
            userProfileCreationService.createUserProfile(event);

            profileCreatedResponseEventDTO.setStatus("success");
            userProfileProducer.sendMessage(profileCreatedResponseEventDTO);

        } catch (RuntimeException e) {

            profileCreatedResponseEventDTO.setStatus("failure");
            userProfileProducer.sendMessage(profileCreatedResponseEventDTO);
            throw new RuntimeException(e);
        }

        System.out.println(event);
    }
}
