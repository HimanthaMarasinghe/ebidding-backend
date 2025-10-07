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
import com.e.bidding.bidding_service.repo.BidRepo;
import com.e.bidding.dtos.ItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    public UserService(WebClient.Builder webClientBuilder, BidRepo bidRepo, @Value("${item.service.url}") String itemServiceUrl) {
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


}