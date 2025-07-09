package com.e.bidding.item_service.dto;

import com.e.bidding.item_service.common.ItemCategory;
import com.e.bidding.item_service.common.ItemCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
    private ItemCondition condition;
    private String description;
    private String locationId;
    private Map<String, Object> specifications;
    private AuctionDTO auction;
}
