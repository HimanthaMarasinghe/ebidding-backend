package com.e.bidding.item_service.kafka;

import com.e.bidding.dtos.LocationDTO;
import com.e.bidding.item_service.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NewLocationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewLocationConsumer.class);

    private final LocationService locationService;

    public NewLocationConsumer(LocationService locationService) {
        this.locationService = locationService;
    }

    @KafkaListener(topics = "new_location_topic", groupId = "item")
    public void consume(LocationDTO event) {
        LOGGER.info(String.format("Location adding from user service => %s", event.toString()));
        System.out.println("33333333333333333333333333333333333333333");
        System.out.println(event.getName());
        System.out.println("33333333333333333333333333333333333333333");
        locationService.addLocation(event);
    }
}
