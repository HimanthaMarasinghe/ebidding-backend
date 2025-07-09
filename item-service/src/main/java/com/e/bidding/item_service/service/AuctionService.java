package com.e.bidding.item_service.service;

import com.e.bidding.item_service.dto.AuctionDTO;
import com.e.bidding.item_service.model.Auction;
import com.e.bidding.item_service.model.Item;
import com.e.bidding.item_service.repo.AuctionRepo;
import com.e.bidding.item_service.repo.ItemRepo;
import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuctionService {
    private final AuctionRepo auctionRepo;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;

    public AuctionService(AuctionRepo auctionRepo, ModelMapper modelMapper, EntityManager entityManager) {
        this.auctionRepo = auctionRepo;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
    }

    public List<AuctionDTO> scheduleAll(List<AuctionDTO> auctionDTOs) {
        List<Auction> auctions = auctionDTOs.stream().map(dto -> {
            Auction auction = modelMapper.map(dto, Auction.class);

            // Create a lightweight Item with only id (no DB fetch)
            Item item = entityManager.getReference(Item.class, dto.getId());

            auction.setItem(item);
            auction.setId(null);  // Ensure JPA treats as new insert

            return auction;
        }).collect(Collectors.toList());

        List<Auction> savedAuctions = auctionRepo.saveAll(auctions);

        return savedAuctions.stream()
                .map(a -> modelMapper.map(a, AuctionDTO.class))
                .collect(Collectors.toList());
    }


}
