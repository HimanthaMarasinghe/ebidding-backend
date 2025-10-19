package com.e.bidding.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponseDTO {
    private AnalyticsDTO analytics;
    private List<CategoryBreakdownDTO> categoryBreakdown;
    private List<WeeklyPerformanceDTO> weeklyPerformance;
    private List<TopItemDTO> topItems;
}

