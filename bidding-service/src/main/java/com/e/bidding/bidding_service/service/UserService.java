package com.e.bidding.bidding_service.service;

//import com.e.bidding.springSecurity.model.Users;
//import com.e.bidding.springSecurity.repo.UserRepo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    public List<ItemDTO> getItemsForUser(String userName){
        List<Bid> BidList = bidRepo.findByBidderUserName(userName);
        StringBuilder idParams = new StringBuilder();
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
            return Arrays.asList(response);
        }
        catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }


}