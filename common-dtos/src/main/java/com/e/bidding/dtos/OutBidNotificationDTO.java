package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutBidNotificationDTO {
    private String prevBidder;
    private Integer itemId;
    private long prevAmount;
    private long newAmount;
}
