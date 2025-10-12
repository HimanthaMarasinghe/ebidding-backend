package com.e.bidding.bidding_service.repo;

import com.e.bidding.bidding_service.model.AuctionWinner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionWinnerRepo extends JpaRepository <AuctionWinner,Integer> {
    AuctionWinner findFirstByItemIdAndWinnerUserName(Integer itemId,String winnerUserName);
}
