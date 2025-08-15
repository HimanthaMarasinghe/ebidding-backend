package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveItemBidValidationDTO {
    private int startingBid;
    private int increment;
    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
}
