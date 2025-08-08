package com.e.bidding.item_service.kafka;

import com.e.bidding.dtos.LocationDTO;
import com.e.bidding.dtos.NewLocationWithYardManDTO;
import com.e.bidding.item_service.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NewLocationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewLocationConsumer.class);

    private final LocationService locationService;
    private final NewLocationIdProducer newLocationIdProducer;

    public NewLocationConsumer(LocationService locationService, NewLocationIdProducer newLocationIdProducer) {
        this.locationService = locationService;
        this.newLocationIdProducer = newLocationIdProducer;
    }

    @KafkaListener(topics = "new_location_topic", groupId = "item")
    public void consume(NewLocationWithYardManDTO event) {
        LOGGER.info(String.format("Location adding from user service => %s", event.toString()));
        NewLocationWithYardManDTO respond = new NewLocationWithYardManDTO();
        respond.setId(event.getId());
        try {
            LocationDTO newLocation = locationService.addLocation(event.getLocation());
            newLocation.setName(null);
            newLocation.setAddress(null);
            newLocation.setLongitude(null);
            newLocation.setLatitude(null);
            respond.setLocation(newLocation);
            respond.setSuccess(true);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            respond.setSuccess(false);
        }
        newLocationIdProducer.sendNewLocationId(respond);
    }
}
