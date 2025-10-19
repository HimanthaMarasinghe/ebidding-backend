package com.e.bidding.bidding_service.controller;

import com.e.bidding.bidding_service.dto.AutoBidDTO;
import com.e.bidding.bidding_service.dto.BidDTO;
import com.e.bidding.bidding_service.dto.BiddingDetailsResponseDTO;
import com.e.bidding.bidding_service.dto.HighestBidDTO;
import com.e.bidding.bidding_service.service.BidService;
import com.e.bidding.dtos.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/bs/v1")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @GetMapping("/getBiddingDetails/{itemId}")
    public BiddingDetailsResponseDTO getBiddingHistory(@PathVariable int itemId) {
        BiddingDetailsResponseDTO response = new BiddingDetailsResponseDTO();
        response.setBidHistoryItems(bidService.getBiddingHistory(itemId));
        response.setMyAutoBid(bidService.getMyAutoBid(itemId));
        return response;
    }

    @PostMapping("/bid")
    public ResponseDTO<Integer> bid(@RequestBody BidDTO bidDTO) {
        return bidService.addBid(bidDTO, false);
    }

    @PostMapping("/autoBid")
    public ResponseDTO<AutoBidDTO> autoBid(@RequestBody AutoBidDTO autoBidDTO) {
        return bidService.setAutoBid(autoBidDTO);
    }

    @GetMapping("/getHighestBid/{itemId}")
    public ResponseEntity<HighestBidDTO> getHighestBidForItem(@PathVariable Integer itemId) {
        if (itemId != null) {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return ResponseEntity.ok(bidService.getHighestBidForItem(itemId,username));
        }
        return ResponseEntity.badRequest().body(null);

    }
}
