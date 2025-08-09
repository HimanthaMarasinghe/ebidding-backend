package com.e.bidding.bidding_service.repo;

import com.e.bidding.bidding_service.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepo extends JpaRepository<Bid, Integer> {
}
