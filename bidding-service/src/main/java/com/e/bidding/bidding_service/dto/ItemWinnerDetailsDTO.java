package com.e.bidding.bidding_service.dto;

import com.e.bidding.dtos.ItemDTO;
import com.e.bidding.dtos.UserProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ItemWinnerDetailsDTO {
    private String winnerUserName;
    private Integer itemId;
    private Long amount;
    private boolean isClaimed;
    private Integer winnerPlace;
    private ItemDTO itemDetails;
    private UserProfileDTO userDetails;

}
