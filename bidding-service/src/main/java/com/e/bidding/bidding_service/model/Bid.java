package com.e.bidding.bidding_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 💡 This enables auto-increment
    private Integer bidId;

    private String bidderUserName;
    private Integer itemId;
    private Double amount;
    private LocalDateTime bidTime;
}
