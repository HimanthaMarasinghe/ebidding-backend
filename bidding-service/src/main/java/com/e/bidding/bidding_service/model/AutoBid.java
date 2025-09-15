package com.e.bidding.bidding_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AutoBid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 💡 This enables auto-increment
    private Integer autoBidId;

    private String bidderUserName;
    private Integer itemId;
    private long amount;
    private LocalDateTime bidTime;
}
