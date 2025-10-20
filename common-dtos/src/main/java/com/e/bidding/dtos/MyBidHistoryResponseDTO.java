package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MyBidHistoryResponseDTO {
    private ItemDTO itemDetails;
    private String Status;
    private Long myBid;
}
