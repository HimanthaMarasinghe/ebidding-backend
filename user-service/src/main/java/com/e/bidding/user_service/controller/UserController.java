package com.e.bidding.user_service.controller;

import com.e.bidding.user_service.model.AuctionManager;
import com.e.bidding.user_service.model.Bidder;
import com.e.bidding.user_service.model.UserProfile;
import com.e.bidding.user_service.model.YardManager;
import com.e.bidding.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/us/v1")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfile> getUserById(@PathVariable Integer userId) {
        log.info("GET /us/v1/user/{} called", userId);
        Optional<UserProfile> user = userService.getUserById(userId);
        log.info("Service returned: {}", user);

        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        log.info("GET /us/v1/users called");
        List<UserProfile> users = userService.getAllUsers();
        log.info("Service returned {} users", users.size());

        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserProfile>> getUsersByRole(@PathVariable String role) {
        log.info("GET /us/v1/users/role/{} called", role);
        List<UserProfile> users = userService.getUsersByRole(role);
        log.info("Service returned {} users with role {}", users.size(), role);

        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }

    // Bidder endpoints
    @GetMapping("/bidders")
    public ResponseEntity<List<Bidder>> getAllBidders() {
        log.info("GET /us/v1/bidders called");
        try {
            List<Bidder> bidders = userService.getAllBidders();
            log.info("Service returned {} bidders", bidders.size());

            if (bidders.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(bidders, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Error fetching bidders: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/bidders/{bidderId}")
    public ResponseEntity<Bidder> getBidderById(@PathVariable Integer bidderId) {
        log.info("GET /us/v1/bidders/{} called", bidderId);
        try {
            Optional<Bidder> bidder = userService.getBidderById(bidderId);
            log.info("Service returned: {}", bidder);

            if (bidder.isPresent()) {
                return new ResponseEntity<>(bidder.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error fetching bidder {}: ", bidderId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Auction Manager endpoints
    @GetMapping("/auction-managers")
    public ResponseEntity<List<AuctionManager>> getAllAuctionManagers() {
        log.info("GET /us/v1/auction-managers called");
        try {
            List<AuctionManager> auctionManagers = userService.getAllAuctionManagers();
            log.info("Service returned {} auction managers", auctionManagers.size());

            if (auctionManagers.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(auctionManagers, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Error fetching auction managers: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/auction-managers/{auctionManagerId}")
    public ResponseEntity<AuctionManager> getAuctionManagerById(@PathVariable Integer auctionManagerId) {
        log.info("GET /us/v1/auction-managers/{} called", auctionManagerId);
        try {
            Optional<AuctionManager> auctionManager = userService.getAuctionManagerById(auctionManagerId);
            log.info("Service returned: {}", auctionManager);

            if (auctionManager.isPresent()) {
                return new ResponseEntity<>(auctionManager.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error fetching auction manager {}: ", auctionManagerId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Yard Manager endpoints
    @GetMapping("/yard-managers")
    public ResponseEntity<List<YardManager>> getAllYardManagers() {
        log.info("GET /us/v1/yard-managers called");
        try {
            List<YardManager> yardManagers = userService.getAllYardManagers();
            log.info("Service returned {} yard managers", yardManagers.size());

            if (yardManagers.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(yardManagers, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Error fetching yard managers: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/yard-managers/{yardManagerId}")
    public ResponseEntity<YardManager> getYardManagerById(@PathVariable Integer yardManagerId) {
        log.info("GET /us/v1/yard-managers/{} called", yardManagerId);
        try {
            Optional<YardManager> yardManager = userService.getYardManagerById(yardManagerId);
            log.info("Service returned: {}", yardManager);

            if (yardManager.isPresent()) {
                return new ResponseEntity<>(yardManager.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error fetching yard manager {}: ", yardManagerId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getSelfDetails")
    public  ResponseEntity<UserProfile> getBidderDetails(){
        log.info("GET /us/v1/getSelfDetails called");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Extracted username from token: {}", username);

        if(username == null || username.isEmpty()){
            log.warn("Username is empty or null");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            UserProfile profile = userService.getDetailsByUserName(username);
            log.info("Service returned profile for user: {}", username);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error fetching details for user {}: ", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
