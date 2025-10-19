package com.e.bidding.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryBreakdownDTO {
    private String name;
    private long revenue;
    private long profit;
    private int items;
    private double percentage;
}

