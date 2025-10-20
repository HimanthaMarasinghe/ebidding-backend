package com.e.bidding.item_service.service;

import com.e.bidding.item_service.dto.AuctionDTO;
import com.e.bidding.dtos.ResponseDTO;
import com.e.bidding.item_service.dto.ItemDTO;
import com.e.bidding.item_service.model.Auction;
import com.e.bidding.item_service.model.Item;
import com.e.bidding.item_service.repo.AuctionRepo;
import com.e.bidding.item_service.repo.ItemRepo;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AuctionService {
    private final AuctionRepo auctionRepo;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;
    private final ItemRepo itemRepo;
    private final WebClient userWebClient;


    public AuctionService(AuctionRepo auctionRepo, ModelMapper modelMapper, EntityManager entityManager, ItemRepo itemRepo, WebClient.Builder webClientBuilder, @Value("${user.service.url}") String userService) {
        this.auctionRepo = auctionRepo;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
        this.itemRepo = itemRepo;
        this.userWebClient=webClientBuilder.baseUrl(userService).build();
    }

    public ResponseDTO<List<AuctionDTO>> scheduleAll(List<AuctionDTO> auctionDTOs) {
        List<Auction> auctions = auctionDTOs.stream().map(dto -> {
            Auction auction = modelMapper.map(dto, Auction.class);
            auction.setId(null);
            Item item = entityManager.getReference(Item.class, dto.getId());
            auction.setItem(item);
            return auction;
        }).collect(Collectors.toList());

        List<Auction> saved = auctionRepo.saveAll(auctions);

        List<AuctionDTO> result = saved.stream()
                .map(a -> modelMapper.map(a, AuctionDTO.class))
                .collect(Collectors.toList());

        return new ResponseDTO<>(true, result, "Saved successfully");
    }
    public List<ItemDTO> getYardItems(String username) {
        Integer locationID=fetchYard(username);
        if(locationID!=null){
            List <Item> items=itemRepo.findByLocation_Id(locationID);
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            List<Item> Items = items.stream()
                    .filter(item -> item.getAuction() != null
                            && item.getAuction().getStartingTime() != null
                            && item.getAuction().getEndingTime() != null
                            && now.isAfter(item.getAuction().getStartingTime())
                    )    //&& now.isAfter(item.getAuction().getEndingTime()) IMPORTANT : ADD THIS LINE TO GET THE HISTORY OF ENDED ITEMS ONLY
                    .toList();
            List<ItemDTO> ItemDTOs = modelMapper.map(Items, new TypeToken<List<ItemDTO>>() {}.getType());
            ItemDTOs.forEach(ItemDTO::updateStatus);
            return ItemDTOs;
        }
        return Collections.emptyList();

    }

    private Integer fetchYard(String username){
        try {
            return userWebClient.get()
                    .uri("/getAuctionManYard/{username}", username)
                    .retrieve()
                    .bodyToMono(Integer.class)
                    .block();
        } catch (Exception e) {
            log.error("Error calling User Service for username {}: {}", username, e.getMessage());
        }
        return null;
        }
}
