package com.e.bidding.item_service.dto;

import com.e.bidding.dtos.LocationDTO;
import com.e.bidding.item_service.common.ItemCategory;
import com.e.bidding.item_service.common.ItemCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {
    private Integer id;
    private String caseNumber;
    private String title;
    private ItemCategory category;
    private int startingBid;
    private int increment;
    private int valuation;
    private String status;
    private ItemCondition condition;
    private String description;
    private LocationDTO location;
    private List<ItmeSpecsDTO> specs;
    private AuctionDTO auction;
    private List<ItemImageDTO> images;
}
