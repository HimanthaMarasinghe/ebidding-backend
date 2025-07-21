package com.e.bidding.item_service.controller;

import com.e.bidding.dtos.LocationDTO;
import com.e.bidding.item_service.service.LocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/is/v1")
public class LocationControler {
    private final LocationService locationService;

    public LocationControler(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/allLocations")
    public List<LocationDTO> getAllLocations() {
        return locationService.allLocations();
    }
}
