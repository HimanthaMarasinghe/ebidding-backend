package com.e.bidding.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopItemDTO {
    private String name;
    private long finalBid;
    private int bids;
    private String category;
}

