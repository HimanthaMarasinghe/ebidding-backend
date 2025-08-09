package com.e.bidding.bidding_service.controller;

import com.e.bidding.bidding_service.dto.BidDTO;
import com.e.bidding.bidding_service.service.BidService;
import com.e.bidding.dtos.ResponseDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/bs/v1")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping("/bid")
    public ResponseDTO<Integer> bid(@RequestBody BidDTO bidDTO) {
        boolean success = bidService.addBid(bidDTO);
        ResponseDTO<Integer> responseDTO = new ResponseDTO<>();
        responseDTO.setSuccess(success);
        if (success)
            responseDTO.setMessage("Bid added successfully");
        else
            responseDTO.setMessage("Bid addition failed");
        return responseDTO;
    }
}
