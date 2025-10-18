package com.e.bidding.bidding_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class AuctionWinner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 💡 This enables auto-increment
    private Integer winningId;

    private Integer itemId;
    private String winnerUserName;
    private boolean isClaimed;
    private Integer winningPlace;
    private Long bidAmount;

}
