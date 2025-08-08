package com.e.bidding.item_service.service;

import com.e.bidding.dtos.LocationDTO;
import com.e.bidding.item_service.model.Location;
import com.e.bidding.item_service.repo.LocationRepo;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LocationService {
    private final LocationRepo locationRepo;
    private final ModelMapper modelMapper;

    public LocationService(LocationRepo locationRepo, ModelMapper modelMapper) {
        this.locationRepo = locationRepo;
        this.modelMapper = modelMapper;
    }

    public List<LocationDTO> allLocations() {
        List<Location> locations = locationRepo.findAll();
        return modelMapper.map(locations, new TypeToken<List<LocationDTO>>() {}.getType());
    }

    public LocationDTO addLocation(LocationDTO locationDTO) {
        Location location = modelMapper.map(locationDTO, Location.class);
        Location newLocation = locationRepo.save(location);
        return modelMapper.map(newLocation, LocationDTO.class);
    }

    public boolean locationValidate(LocationDTO locationDTO) {
        return locationRepo.existsById(locationDTO.getId());
    }
}
