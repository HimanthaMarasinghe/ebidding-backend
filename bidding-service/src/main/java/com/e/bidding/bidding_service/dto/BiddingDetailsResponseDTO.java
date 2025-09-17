package com.e.bidding.bidding_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiddingDetailsResponseDTO {
    List<BidHistoryItemDTO> bidHistoryItems;
    MyAutoBidDTO myAutoBid;
}
