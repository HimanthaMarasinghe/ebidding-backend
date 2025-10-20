package com.e.bidding.bidding_service.controller;

import com.e.bidding.bidding_service.dto.DepositDTO;
import com.e.bidding.bidding_service.dto.ItemWinnerDetailsDTO;
import com.e.bidding.bidding_service.dto.MyBidsDTO;
import com.e.bidding.bidding_service.model.Deposit;

import com.e.bidding.bidding_service.model.Bid;
import com.e.bidding.bidding_service.repo.BidRepo;
import com.e.bidding.bidding_service.service.AuctionEndService;
import com.e.bidding.bidding_service.service.UserService;
import com.e.bidding.dtos.ItemDTO;
import com.e.bidding.dtos.MyBidHistoryResponseDTO;
import com.e.bidding.dtos.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuctionEndService auctionEndService;

    @Autowired
    BidRepo bidRepo;

    @PostMapping("/hello")
    public String hello(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            System.out.println("Hello endpoint hit with token: " + jwtToken);
            return "hello";
        }
        // No valid JWT, return 401 to trigger refresh
        throw new SecurityException("Missing or invalid JWT token");
    }

    @GetMapping("/getMyBidItems/{username}")
    public ResponseEntity<ArrayList<MyBidsDTO>> getMyBidItems(@PathVariable String username){
        if(username.isEmpty()||username==""){
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok().body(userService.getItemsForUser(username));
    }

    @PostMapping("/makeDeposit")
    public ResponseEntity<String> makeDeposit(@RequestBody DepositDTO deposit) {
        String userName = deposit.getUserName();
        long amount = deposit.getAmount();

        Deposit depositedRequest = userService.makeDeposit(userName, amount);

        System.out.println("Received payment: " + userName + " - " + amount);
        return ResponseEntity.ok("Payment recorded successfully for " + userName);
    }
    //debugging purposes only
    @GetMapping("/testwinner/{itemId}")
    public boolean getAndSetWinner(@PathVariable long itemId){
        String winner=auctionEndService.handleAuctionEnd(itemId);
        boolean state= auctionEndService.checkClaimed(Math.toIntExact(itemId),"200123503222");
        System.out.println(state);

        Optional<Bid> nextHighestBid=bidRepo.findBidByPlace(Math.toIntExact(itemId),Math.toIntExact(2));
        if(nextHighestBid.isPresent()) {
            auctionEndService.handleNewWinnerSet(Math.toIntExact(itemId), nextHighestBid.get().getBidderUserName(), 2, nextHighestBid.get().getAmount(),"200123503222");
        }
        return state;
    }

    @GetMapping("/getMyBiddingHistory/{username}")
    public ResponseEntity<List<MyBidHistoryResponseDTO>> getMyBiddingHistory(@PathVariable String username){
        if(username.isEmpty()||username==""){
            return ResponseEntity.badRequest().body(null);
        }
        List<MyBidHistoryResponseDTO> responseDTOS = userService.getItemHistoryForUser(username);
        if(responseDTOS!=null){
            return ResponseEntity.ok(responseDTOS);

        }
        else {
            return ResponseEntity.ok(Collections.emptyList());
        }

    }

    @GetMapping("/getwinner/{itemId}")
    public ResponseEntity<ItemWinnerDetailsDTO> getWinnerForItem(@PathVariable Integer itemId , HttpServletRequest request){
        if(itemId!=null){
            return ResponseEntity.ok().body(userService.getWinner(itemId,request));
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PutMapping("/ReleaseItem/{itemId}")
    public ResponseDTO<String> ReleaseItem(@PathVariable Integer itemId){
        String AuctionManUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        if(itemId!=null){
           return userService.claimItem(itemId,AuctionManUserName);
        }
        return new ResponseDTO<>(false,"Item id Not found","Item id not found");
    }



}
