package com.e.bidding.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidHistoryItemDTO {
    private Integer bidId;
    private boolean placedByMe = false;
    private Integer itemId;
    private Long amount;
    private LocalDateTime bidTime;
    private boolean autoBid = false;
}
