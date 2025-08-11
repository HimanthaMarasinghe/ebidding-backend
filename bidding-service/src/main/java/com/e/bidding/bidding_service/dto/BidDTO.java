package com.e.bidding.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidDTO {
    private Integer bidId;
    private Integer bidderId;
    private Integer itemId;
    private Double amount;
    private LocalDateTime bidTime;
}
