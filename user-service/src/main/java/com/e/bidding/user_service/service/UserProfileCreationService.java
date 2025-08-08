package com.e.bidding.user_service.service;

import com.e.bidding.dtos.LocationDTO;
import com.e.bidding.dtos.NewLocationWithYardManDTO;
import com.e.bidding.dtos.ProfileCreationEventDTO;
import com.e.bidding.dtos.UserAddingDTO;
import com.e.bidding.user_service.kafka.NewLocationProducer;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileCreationService {

    @Autowired
    private UserProfileRepo userProfileRepo;

    @Autowired
    private BidderRepo bidderRepo;

    @Autowired
    private AuctionManagerRepo auctionManagerRepo;

    @Autowired
    private YardManagerRepo yardManagerRepo;

    @Autowired
    private NewLocationProducer newLocationProducer;

    @Transactional
    public void createUserProfile(ProfileCreationEventDTO profileCreationEventDTO) {

        Bidder bidder = new Bidder();

        bidder.setUsername(profileCreationEventDTO.getUsername());
        bidder.setEmail(profileCreationEventDTO.getEmail());
        bidder.setFirstName(profileCreationEventDTO.getFirst_name());
        bidder.setLastName(profileCreationEventDTO.getLast_name());
        bidder.setPrimaryPhone(profileCreationEventDTO.getPrimary_phone());
        bidder.setSecondaryPhone(profileCreationEventDTO.getSecondary_phone());
        bidder.setDate_of_birth(profileCreationEventDTO.getDate_of_birth());
        bidder.setRole(profileCreationEventDTO.getRole());
        bidder.setUser_image_url(profileCreationEventDTO.getUser_image_url());
        bidder.setNic_image_url(profileCreationEventDTO.getNic_image_url());

        bidderRepo.save(bidder);
    }


    @Transactional
    public UserProfile addUserProfile(UserAddingDTO userAddingDTO){
        String role = userAddingDTO.getRole().toLowerCase();

        switch (role){
            case "auction_manager" -> {
                AuctionManager auctionManager = new AuctionManager();
                mapCommonFields(auctionManager, userAddingDTO);
//                auctionManager.setDesignation("aaa");
//                auctionManager.setAuction_center("Colombo");
                return auctionManagerRepo.save(auctionManager);
            }

            case "yard_manager" -> {
                YardManager yardManager = new YardManager();
                mapCommonFields(yardManager, userAddingDTO);
                yardManager.setYard_id(-1);
                /*
                 * Sets the yard_id to -1 to represent a pending status. This ID will be updated
                 * with the new auto-incremented yard ID once the item service has successfully
                 * created the new yard. If the item service fails, this value will be set to
                 * null. The pending status is critical because it prevents a new yard assignment
                 * made by an administrator from being overwritten by the asynchronous update
                 * from the item service.
                 */
                UserProfile newYardMan = yardManagerRepo.save(yardManager);
                if(userAddingDTO.getLocation().getId() == null) {
                    NewLocationWithYardManDTO newLocation = getNewLocationWithYardManDTO(userAddingDTO, newYardMan);
                    newLocationProducer.sendNewLocation(newLocation);
                } else {
                    LocationDTO locationDTO = new LocationDTO();
                    locationDTO.setId(userAddingDTO.getLocation().getId());
                    //validate using kafka
                }
                return newYardMan;
            }
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    private static NewLocationWithYardManDTO getNewLocationWithYardManDTO(UserAddingDTO userAddingDTO, UserProfile newYardMan) {
        NewLocationWithYardManDTO newLocation = new NewLocationWithYardManDTO();
        LocationDTO newLocationDTO = new LocationDTO();
        newLocationDTO.setName(userAddingDTO.getLocation().getName());
        newLocationDTO.setLatitude(userAddingDTO.getLocation().getLatitude());
        newLocationDTO.setLongitude(userAddingDTO.getLocation().getLongitude());
        newLocationDTO.setAddress(userAddingDTO.getLocation().getAddress());
        newLocation.setLocation(newLocationDTO);
        newLocation.setId(newYardMan.getId());
        return newLocation;
    }

    private void mapCommonFields(UserProfile userProfile, UserAddingDTO dto) {
        userProfile.setUsername(dto.getUsername());
        userProfile.setEmail(dto.getEmail());
        userProfile.setFirstName(dto.getFirst_name());
        userProfile.setLastName(dto.getLast_name());
        userProfile.setPrimaryPhone(dto.getPrimary_phone());
        userProfile.setSecondaryPhone(dto.getSecondary_phone());
        userProfile.setDate_of_birth(dto.getDate_of_birth());
        userProfile.setRole(dto.getRole());
    }

    public void rollBackUserAdding(String username){

        UserProfile userProfile = userProfileRepo.findByUsername(username);
        if (userProfile != null) {
            //userProfileRepo.delete(userProfile);
            System.out.println("Rolled back registration for user: " + username);
        }
    }

    private void createBidderProfile(String nic, String email, String phone_number, int age) {

    }
}
