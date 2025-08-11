package com.e.bidding.bidding_service.service;

import com.e.bidding.bidding_service.dto.BidDTO;
import com.e.bidding.bidding_service.model.Bid;
import com.e.bidding.bidding_service.repo.BidRepo;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class BidService {
    private final BidRepo bidRepo;
    private final ModelMapper modelMapper;

    public BidService(BidRepo bidRepo, ModelMapper modelMapper) {
        this.bidRepo = bidRepo;
        this.modelMapper = modelMapper;
    }

    public boolean addBid(BidDTO bidDTO) {
        try {
            bidDTO.setBidTime(LocalDateTime.now(ZoneOffset.UTC));
            // Items status and user's maximum bidding amount Validations goes here
            bidRepo.save(modelMapper.map(bidDTO, Bid.class));
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}
