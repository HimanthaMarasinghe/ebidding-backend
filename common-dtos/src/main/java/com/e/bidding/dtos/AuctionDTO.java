package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuctionDTO {
    private Integer id;

    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
}
