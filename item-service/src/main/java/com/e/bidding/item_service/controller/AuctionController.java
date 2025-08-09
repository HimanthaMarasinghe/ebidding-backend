package com.e.bidding.item_service.controller;

import com.e.bidding.item_service.dto.AuctionDTO;
import com.e.bidding.dtos.ResponseDTO;
import com.e.bidding.item_service.service.AuctionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/is/v1")
public class AuctionController {
    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping("/schedule")
    public ResponseDTO<List<AuctionDTO>> schedule(@RequestBody List<AuctionDTO> auctions) {
        return auctionService.scheduleAll(auctions);
    }
}
