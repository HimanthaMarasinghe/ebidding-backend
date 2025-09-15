package com.e.bidding.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoBidDTO {
    private Integer autoBidId;
    private String bidderUserName;
    private Integer itemId;
    private long amount;
    private LocalDateTime bidTime;
}
