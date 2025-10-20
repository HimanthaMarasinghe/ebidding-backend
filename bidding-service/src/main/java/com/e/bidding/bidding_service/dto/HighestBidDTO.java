package com.e.bidding.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HighestBidDTO {
    private Integer itemID;
    private Long highestAmount;
    private Integer totalBids;
    private boolean isPlacedByMe;
}
