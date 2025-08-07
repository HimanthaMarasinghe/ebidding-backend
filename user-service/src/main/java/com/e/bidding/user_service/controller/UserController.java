package com.e.bidding.user_service.controller;

import com.e.bidding.user_service.model.AuctionManager;
import com.e.bidding.user_service.model.Bidder;
import com.e.bidding.user_service.model.UserProfile;
import com.e.bidding.user_service.model.YardManager;
import com.e.bidding.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/us/v1")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfile> getUserById(@PathVariable Integer userId) {
        Optional<UserProfile> user = userService.getUserById(userId);

        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        List<UserProfile> users = userService.getAllUsers();
        
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserProfile>> getUsersByRole(@PathVariable String role) {
        List<UserProfile> users = userService.getUsersByRole(role);

        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }

    // Bidder endpoints
    @GetMapping("/bidders")
    public ResponseEntity<List<Bidder>> getAllBidders() {
        List<Bidder> bidders = userService.getAllBidders();

        if (bidders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(bidders, HttpStatus.OK);
        }
    }

    @GetMapping("/bidders/{bidderId}")
    public ResponseEntity<Bidder> getBidderById(@PathVariable Integer bidderId) {
        Optional<Bidder> bidder = userService.getBidderById(bidderId);

        if (bidder.isPresent()) {
            return new ResponseEntity<>(bidder.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Auction Manager endpoints
    @GetMapping("/auction-managers")
    public ResponseEntity<List<AuctionManager>> getAllAuctionManagers() {
        List<AuctionManager> auctionManagers = userService.getAllAuctionManagers();

        if (auctionManagers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(auctionManagers, HttpStatus.OK);
        }
    }

    @GetMapping("/auction-managers/{auctionManagerId}")
    public ResponseEntity<AuctionManager> getAuctionManagerById(@PathVariable Integer auctionManagerId) {
        Optional<AuctionManager> auctionManager = userService.getAuctionManagerById(auctionManagerId);

        if (auctionManager.isPresent()) {
            return new ResponseEntity<>(auctionManager.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Yard Manager endpoints
    @GetMapping("/yard-managers")
    public ResponseEntity<List<YardManager>> getAllYardManagers() {
        List<YardManager> yardManagers = userService.getAllYardManagers();

        if (yardManagers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(yardManagers, HttpStatus.OK);
        }
    }

    @GetMapping("/yard-managers/{yardManagerId}")
    public ResponseEntity<YardManager> getYardManagerById(@PathVariable Integer yardManagerId) {
        Optional<YardManager> yardManager = userService.getYardManagerById(yardManagerId);

        if (yardManager.isPresent()) {
            return new ResponseEntity<>(yardManager.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
