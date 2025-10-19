package com.e.bidding.bidding_service.service;

//import com.e.bidding.springSecurity.model.Users;
//import com.e.bidding.springSecurity.repo.UserRepo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.e.bidding.bidding_service.dto.MyBidsDTO;
import com.e.bidding.bidding_service.model.Bid;
import com.e.bidding.bidding_service.model.Deposit;
import com.e.bidding.bidding_service.repo.BidRepo;
import com.e.bidding.bidding_service.repo.DepositRepo;
import com.e.bidding.dtos.ItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final WebClient webClient;
    private final BidRepo bidRepo;
    private final DepositRepo depositRepo;

    public UserService(WebClient.Builder webClientBuilder, BidRepo bidRepo, @Value("${item.service.url}") String itemServiceUrl, DepositRepo depositRepo) {
        this.depositRepo = depositRepo;
        this.webClient = webClientBuilder.baseUrl(itemServiceUrl).build();
        this.bidRepo = bidRepo;
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

}