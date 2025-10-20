package com.e.bidding.bidding_service.repo;

import com.e.bidding.bidding_service.model.AuctionWinner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionWinnerRepo extends JpaRepository <AuctionWinner,Integer> {
    AuctionWinner findFirstByItemIdAndWinnerUserName(Integer itemId,String winnerUserName);

    List<AuctionWinner> findDistinctByWinnerUserName(String winnerUserName);

    Optional<AuctionWinner> findFirstByItemIdAndIsDiscardedFalseOrderByWinningIdAsc(Integer itemId);

}
