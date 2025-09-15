package com.e.bidding.bidding_service.repo;

import com.e.bidding.bidding_service.model.AutoBid;
import com.e.bidding.bidding_service.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutoBidRepo extends JpaRepository<AutoBid, Integer> {

    Optional<AutoBid> findTopByItemIdOrderByAmountDesc(Integer itemId);
}
