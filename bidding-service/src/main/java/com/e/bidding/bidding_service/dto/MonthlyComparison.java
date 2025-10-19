package com.e.bidding.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyComparison {
    private double revenueChange;
    private double profitChange;
    private double auctionsChange;
    private double bidsChange;
}

