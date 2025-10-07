package com.e.bidding.bidding_service.dto;

import com.e.bidding.dtos.ItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyBidsDTO {
    private ItemDTO itemDTO;
    private long currentHighest;
    private long mybid;
    private long bidCount;
}
