package com.e.bidding.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsDTO {
    private long totalRevenue;
    private long totalProfit;
    private int totalAuctions;
    private int totalBids;
    private int totalItems;
    private double successRate;
    private double averageBidPerItem;
    private String topCategory;
    private MonthlyComparison monthlyComparison;
}

