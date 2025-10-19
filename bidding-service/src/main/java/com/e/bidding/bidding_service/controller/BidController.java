package com.e.bidding.bidding_service.controller;

import com.e.bidding.bidding_service.dto.AnalyticsResponseDTO;
import com.e.bidding.bidding_service.dto.AutoBidDTO;
import com.e.bidding.bidding_service.dto.BidDTO;
import com.e.bidding.bidding_service.dto.BiddingDetailsResponseDTO;
import com.e.bidding.bidding_service.service.AnalyticsService;
import com.e.bidding.bidding_service.service.BidService;
import com.e.bidding.dtos.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@CrossOrigin
@RequestMapping("/bs/v1")
public class BidController {

    private final BidService bidService;
    private final AnalyticsService analyticsService;

    public BidController(BidService bidService, AnalyticsService analyticsService) {
        this.bidService = bidService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/getBiddingDetails/{itemId}")
    public BiddingDetailsResponseDTO getBiddingHistory(@PathVariable int itemId) {
        BiddingDetailsResponseDTO response = new BiddingDetailsResponseDTO();
        response.setBidHistoryItems(bidService.getBiddingHistory(itemId));
        response.setMyAutoBid(bidService.getMyAutoBid(itemId));
        return response;
    }

    @PostMapping("/bid")
    public ResponseDTO<Long> bid(@RequestBody BidDTO bidDTO) {
        return bidService.addBid(bidDTO, false);
    }

    @PostMapping("/autoBid")
    public ResponseDTO<AutoBidDTO> autoBid(@RequestBody AutoBidDTO autoBidDTO) {
        return bidService.setAutoBid(autoBidDTO);
    }

    @GetMapping("/health")
    public ResponseDTO<String> healthCheck() {
        return new ResponseDTO<>(true, "Database connected successfully", "Bidding service is running");
    }

    @GetMapping("/analytics")
    public AnalyticsResponseDTO getAnalytics(
        @RequestParam(defaultValue = "0") int month,
        @RequestParam(defaultValue = "2025") int year
    ) {
        // If month is 0, use current month
        if (month == 0) {
            month = LocalDateTime.now().getMonthValue();
        }
        return analyticsService.getMonthlyAnalytics(month, year);
    }
}
