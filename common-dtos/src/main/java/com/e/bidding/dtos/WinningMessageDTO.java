package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WinningMessageDTO {
    private Integer itemId;
    private String winner;
    private Long bidAmount;
    private Integer winningPlace;
}
