package com.e.bidding.item_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {
    private int id;
    private String name;
    private String description;
    private int startingPrice;
    private String storePlace;
}
