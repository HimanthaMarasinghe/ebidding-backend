package com.e.bidding.user_service.repo;

import com.e.bidding.user_service.model.AuctionManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionManagerRepo extends JpaRepository<AuctionManager, Integer> {

}
