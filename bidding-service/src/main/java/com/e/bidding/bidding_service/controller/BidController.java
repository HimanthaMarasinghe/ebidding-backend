package com.e.bidding.bidding_service.controller;

import com.e.bidding.bidding_service.dto.BidDTO;
import com.e.bidding.bidding_service.dto.BidHistoryItemDTO;
import com.e.bidding.bidding_service.service.BidService;
import com.e.bidding.dtos.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/bs/v1")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @GetMapping("/getBiddingHistory/{itemId}")
    public List<BidHistoryItemDTO> getBiddingHistory(@PathVariable int itemId) {
        return bidService.getBiddingHistory(itemId);
    }

    @PostMapping("/bid")
    public ResponseDTO<Integer> bid(@RequestBody BidDTO bidDTO) {
        return bidService.addBid(bidDTO);
    }
}
