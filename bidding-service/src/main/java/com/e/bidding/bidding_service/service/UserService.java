package com.e.bidding.bidding_service.service;

//import com.e.bidding.springSecurity.model.Users;
//import com.e.bidding.springSecurity.repo.UserRepo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.e.bidding.bidding_service.dto.MyBidsDTO;
import com.e.bidding.bidding_service.model.AuctionWinner;
import com.e.bidding.bidding_service.model.Bid;
import com.e.bidding.bidding_service.model.Deposit;
import com.e.bidding.bidding_service.repo.AuctionWinnerRepo;
import com.e.bidding.bidding_service.repo.BidRepo;
import com.e.bidding.bidding_service.repo.DepositRepo;
import com.e.bidding.dtos.ItemDTO;
import com.e.bidding.dtos.MyBidHistoryResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class UserService {
    private final WebClient webClient;
    private final BidRepo bidRepo;
    private final DepositRepo depositRepo;
    private final AuctionWinnerRepo auctionWinnerRepo;

    public UserService(WebClient.Builder webClientBuilder, BidRepo bidRepo, @Value("${item.service.url}") String itemServiceUrl, DepositRepo depositRepo,AuctionWinnerRepo auctionWinnerRepo) {
        this.depositRepo = depositRepo;
        this.webClient = webClientBuilder.baseUrl(itemServiceUrl).build();
        this.bidRepo = bidRepo;
        this.auctionWinnerRepo=auctionWinnerRepo;
    }

    public String hello(){
        return "Helloo";
    }

    public ArrayList<MyBidsDTO> getItemsForUser(String userName){
        List<Bid> BidList = bidRepo.findByBidderUserName(userName);
        StringBuilder idParams = new StringBuilder();
        ArrayList<MyBidsDTO> myBidsDTOS=new ArrayList<>();
        for(Bid bid:BidList){
            idParams.append(bid.getItemId()).append(",");
        }
        if (!idParams.isEmpty()) {
            idParams.setLength(idParams.length() - 1); // trims the last comma
        }
        try{
            ItemDTO [] response=webClient.get()
                    .uri("/getActiveItemsByIDs/"+idParams)
                    .retrieve()
                    .bodyToMono(ItemDTO[].class)
                    .block();
            assert response != null;
            for(ItemDTO item : response){

                Integer itemId=item.getId();
                long currentHighest=bidRepo.findTopByItemIdOrderByAmountDesc(itemId).get().getAmount();
                long usercurrentHighest=bidRepo.findTopByItemIdAndBidderUserNameOrderByAmountDesc(itemId,userName).get().getAmount();
                long bidCount=bidRepo.countByItemId(itemId);
                MyBidsDTO myBidsDTO=new MyBidsDTO(item,currentHighest,usercurrentHighest,bidCount);
                myBidsDTOS.add(myBidsDTO);
            }

            return myBidsDTOS;
        }
        catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    @Transactional
    public Deposit makeDeposit(String userName, long amount) {

        Deposit latestDepositedAmount = depositRepo.findFirstByUserNameOrderByBidTimeDesc(userName);
        System.out.println(latestDepositedAmount);

        long newAmount;
        if (latestDepositedAmount != null) {
            newAmount = latestDepositedAmount.getAmount() + amount;
        } else {
            // No previous deposit
            newAmount = amount;
        }

        //create new deposit record
        Deposit deposit = new Deposit();
        deposit.setUserName(userName);
        deposit.setAmount(newAmount);
        deposit.setBidTime(LocalDateTime.now());

        System.out.println("Saving deposit with amount: " + newAmount);

        try {
            return depositRepo.save(deposit);
        } catch (Exception e) {
            System.err.println("Failed to save deposit for user " + userName + ": " + e.getMessage());
            throw new RuntimeException("Could not make deposit for user " + userName, e);
        }
    }

    public List<MyBidHistoryResponseDTO> getItemHistoryForUser(String username){
        List<Bid> BidList = bidRepo.findByBidderUserName(username);
        // Null/empty check for BidList
        if (BidList == null || BidList.isEmpty()) {
            log.warn("No bids found for user: {}", username);
            return Collections.emptyList();
        }
        List<AuctionWinner> wonItems = auctionWinnerRepo.findDistinctByWinnerUserName(username);
        if (wonItems == null) {
            log.warn("Won items list is null for user: {}", username);
            wonItems = new ArrayList<>();
        }
        log.info("Before filtering: {}", wonItems);

        List<Integer> wonItemIds = wonItems.stream().map(a -> a != null ? a.getItemId() : null).filter(Objects::nonNull).toList();
        List<Integer> lostItemIds = new ArrayList<>();

        for (Bid bid : BidList) {
            if (bid != null && bid.getItemId() != null && !wonItemIds.contains(bid.getItemId()) && !lostItemIds.contains(bid.getItemId())) {
                lostItemIds.add(bid.getItemId());
            }
        }

        // Create a list for discarded items
        List<AuctionWinner> discardedItems = new ArrayList<>();
        List<AuctionWinner> claimedItems = new ArrayList<>();

        // Use an iterator to safely remove items while iterating
        Iterator<AuctionWinner> iterator = wonItems.iterator();
        while (iterator.hasNext()) {
            AuctionWinner item = iterator.next();
            if (item == null) continue;
            if (item.isDiscarded()) {
                discardedItems.add(item);  // save discarded
                iterator.remove();         // remove from won list
                continue;
            }
            if (item.isClaimed()) {
                claimedItems.add(item);
                iterator.remove();
            }
        }

        List<Integer> discardedItemIds = discardedItems.stream()
                .map(a -> a != null ? a.getItemId() : null)
                .filter(Objects::nonNull)
                .toList();

        List<Integer> claimedItemIds = claimedItems.stream()
                .map(a -> a != null ? a.getItemId() : null)
                .filter(Objects::nonNull)
                .toList();
        List<Integer> wonUnclaimedItemIds = wonItems.stream()
                .map(a -> a != null ? a.getItemId() : null)
                .filter(Objects::nonNull)
                .toList();

        log.info("Won But Unclaimed: {}", wonUnclaimedItemIds);
        log.info("Won But Discarded items: {}", discardedItemIds);
        log.info("Won and Claimed items : {}", claimedItemIds);
        log.info("Lost items: {}", lostItemIds);

        // Collect unique item IDs from BidList
        Set<Integer> uniqueItemIds = new HashSet<>();
        for (Bid bid : BidList) {
            if (bid != null && bid.getItemId() != null) {
                uniqueItemIds.add(bid.getItemId());
            }
        }
        // Build the comma-separated string
        StringBuilder idParams = new StringBuilder();
        for (Integer itemId : uniqueItemIds) {
            idParams.append(itemId).append(",");
        }
        if (!idParams.isEmpty()) {
            idParams.setLength(idParams.length() - 1); // remove last comma
        }
        // Check if uniqueItemIds is empty after processing
        if (uniqueItemIds.isEmpty()) {
            log.warn("No unique item IDs found for user: {}", username);
            return Collections.emptyList();
        }
        ArrayList<MyBidHistoryResponseDTO> myBidsDTOS = new ArrayList<>();
        log.info("Item IDs to get Item service call (unique): {}", idParams);
        ItemDTO[] response = null;
        try {
            response = webClient.get()
                    .uri("/getItemsByIDs/" + idParams)
                    .retrieve()
                    .bodyToMono(ItemDTO[].class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching items from item service: {}", e.getMessage());
            return Collections.emptyList();
        }
        if (response == null || response.length == 0) {
            log.warn("Item service returned no data for user: {}", username);
            return Collections.emptyList();
        }
        for (ItemDTO item : response) {
            if (item == null) continue;
            MyBidHistoryResponseDTO myBidHistoryResponseDTO = new MyBidHistoryResponseDTO();
            myBidHistoryResponseDTO.setItemDetails(item);

            Integer itemID = item.getId();
            Long amount = null;

            Optional<AuctionWinner> discardedMatch = discardedItems.stream()
                    .filter(a -> a != null && a.getItemId() != null && a.getItemId().equals(itemID))
                    .findFirst();

            Optional<AuctionWinner> claimedMatch = claimedItems.stream()
                    .filter(a -> a != null && a.getItemId() != null && a.getItemId().equals(itemID))
                    .findFirst();

            Optional<AuctionWinner> wonMatch = wonItems.stream()
                    .filter(a -> a != null && a.getItemId() != null && a.getItemId().equals(itemID))
                    .findFirst();

            // Null-safe bidAmount extraction
            AuctionWinner match = null;
            if (discardedMatch.isPresent()) {
                match = discardedMatch.orElse(null);
            } else if (claimedMatch.isPresent()) {
                match = claimedMatch.orElse(null);
            } else if (wonMatch.isPresent()) {
                match = wonMatch.orElse(null);
            }

            if (match != null && match.getBidAmount() != null) {
                amount = match.getBidAmount();
            }

            if (amount != null) {
                myBidHistoryResponseDTO.setMyBid(amount);
            } else {
                myBidHistoryResponseDTO.setMyBid(0L); // fallback
            }


            if (lostItemIds.contains(itemID)) {
                myBidHistoryResponseDTO.setStatus("Lost");
                Optional<Bid> userBid=bidRepo.findTopByItemIdAndBidderUserNameOrderByAmountDesc(itemID,username);
                userBid.ifPresent(bid -> myBidHistoryResponseDTO.setMyBid(bid.getAmount()));
            } else if (claimedItemIds.contains(itemID)) {
                myBidHistoryResponseDTO.setStatus("Claimed");
            } else if (discardedItemIds.contains(itemID)) {
                myBidHistoryResponseDTO.setStatus("Discarded");
            } else if (wonUnclaimedItemIds.contains(itemID)) {
                myBidHistoryResponseDTO.setStatus("Won_Unclaimed");
            }
            myBidsDTOS.add(myBidHistoryResponseDTO);
        }

        return myBidsDTOS;
    }





}