package com.e.bidding.item_service.repo;

import com.e.bidding.item_service.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepo extends JpaRepository<Auction, Integer>  {
}
