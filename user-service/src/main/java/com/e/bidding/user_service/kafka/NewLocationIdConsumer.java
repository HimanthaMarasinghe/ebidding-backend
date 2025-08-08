package com.e.bidding.user_service.kafka;

import com.e.bidding.dtos.NewLocationWithYardManDTO;
import com.e.bidding.dtos.ProfileCreatedResponseEventDTO;
import com.e.bidding.user_service.service.UserProfileCreationService;
import com.e.bidding.user_service.service.YardManService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NewLocationIdConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewLocationIdConsumer.class);
    private final YardManService yardManService;

    public NewLocationIdConsumer(YardManService yardManService) {
        this.yardManService = yardManService;
    }

    @KafkaListener(topics = "new_location_id_topic", groupId = "user_profile")
    public void consume(NewLocationWithYardManDTO event) {
        if(event.isSuccess())
            yardManService.changeThePendingYard(event.getId(), event.getLocation().getId());
        else
            yardManService.changeThePendingYard(event.getId(), null);
    }
}