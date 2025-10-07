package com.e.bidding.user_service.service;

import com.e.bidding.user_service.model.AuctionManager;
import com.e.bidding.user_service.model.Bidder;
import com.e.bidding.user_service.model.UserProfile;
import com.e.bidding.user_service.model.YardManager;
import com.e.bidding.user_service.repo.AuctionManagerRepo;
import com.e.bidding.user_service.repo.BidderRepo;
import com.e.bidding.user_service.repo.UserProfileRepo;
import com.e.bidding.user_service.repo.YardManagerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserProfileRepo userProfileRepo;

    @Autowired
    private BidderRepo bidderRepo;

    @Autowired
    private AuctionManagerRepo auctionManagerRepo;

    @Autowired
    private YardManagerRepo yardManagerRepo;

    public Optional<UserProfile> getUserById(Integer userId) {
        return userProfileRepo.findById(userId);
    }

    public List<UserProfile> getAllUsers() {
        return userProfileRepo.findAll();
    }

    public List<UserProfile> getUsersByRole(String role) {
        return userProfileRepo.findByRole(role);
    }

    // Get all bidders
    public List<Bidder> getAllBidders() {
        return bidderRepo.findAll();
    }

    // Get bidder by ID
    public Optional<Bidder> getBidderById(Integer bidderId) {
        return bidderRepo.findById(bidderId);
    }

    // Get all auction managers
    public List<AuctionManager> getAllAuctionManagers() {
        return auctionManagerRepo.findAll();
    }

    // Get auction manager by ID
    public Optional<AuctionManager> getAuctionManagerById(Integer auctionManagerId) {
        return auctionManagerRepo.findById(auctionManagerId);
    }

    // Get all yard managers
    public List<YardManager> getAllYardManagers() {
        return yardManagerRepo.findAll();
    }

    // Get yard manager by ID
    public Optional<YardManager> getYardManagerById(Integer yardManagerId) {
        return yardManagerRepo.findById(yardManagerId);
    }

    public UserProfile getDetailsByUserName(String username){
        UserProfile userProfile=userProfileRepo.findByUsername(username);
        return userProfile;
    }
}
