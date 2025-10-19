package com.e.bidding.bidding_service.service;

import com.e.bidding.bidding_service.dto.*;
import com.e.bidding.bidding_service.model.Bid;
import com.e.bidding.bidding_service.repo.BidRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private final BidRepo bidRepo;

    public AnalyticsService(BidRepo bidRepo) {
        this.bidRepo = bidRepo;
    }

    public AnalyticsResponseDTO getMonthlyAnalytics(int month, int year) {
        logger.info("Fetching analytics for month: {}, year: {}", month, year);

        // Get all bids for the selected month
        List<Bid> monthlyBids = bidRepo.findBidsByMonthAndYear(month, year);

        // Get previous month's data for comparison
        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear = month == 1 ? year - 1 : year;
        List<Bid> prevMonthBids = bidRepo.findBidsByMonthAndYear(prevMonth, prevYear);

        // Calculate main analytics
        AnalyticsDTO analytics = calculateAnalytics(monthlyBids, prevMonthBids, month, year);

        // Calculate category breakdown (we'll need to fetch item details from item-service)
        List<CategoryBreakdownDTO> categoryBreakdown = calculateCategoryBreakdown(monthlyBids);

        // Calculate weekly performance
        List<WeeklyPerformanceDTO> weeklyPerformance = calculateWeeklyPerformance(monthlyBids);

        // Get top performing items
        List<TopItemDTO> topItems = calculateTopItems(monthlyBids);

        return new AnalyticsResponseDTO(analytics, categoryBreakdown, weeklyPerformance, topItems);
    }

    private AnalyticsDTO calculateAnalytics(List<Bid> monthlyBids, List<Bid> prevMonthBids, int month, int year) {
        // Get winning bids (highest bid per item)
        Map<Integer, Bid> winningBids = monthlyBids.stream()
            .collect(Collectors.toMap(
                Bid::getItemId,
                bid -> bid,
                (existing, replacement) -> existing.getAmount() > replacement.getAmount() ? existing : replacement
            ));

        long totalRevenue = winningBids.values().stream()
            .mapToLong(Bid::getAmount)
            .sum();

        // Profit calculation (assuming 25% profit margin - adjust as needed)
        long totalProfit = (long) (totalRevenue * 0.25);

        int totalItems = winningBids.size();
        int totalBids = monthlyBids.size();

        // Calculate distinct auctions (items with bids)
        int totalAuctions = (int) monthlyBids.stream()
            .map(Bid::getItemId)
            .distinct()
            .count();

        // Success rate (items that received bids)
        double successRate = totalItems > 0 ? (double) totalAuctions / totalItems * 100 : 0;

        // Average bids per item
        double averageBidPerItem = totalItems > 0 ? (double) totalBids / totalItems : 0;

        // Calculate monthly comparison
        MonthlyComparison comparison = calculateMonthlyComparison(monthlyBids, prevMonthBids);

        AnalyticsDTO analytics = new AnalyticsDTO();
        analytics.setTotalRevenue(totalRevenue);
        analytics.setTotalProfit(totalProfit);
        analytics.setTotalAuctions(totalAuctions);
        analytics.setTotalBids(totalBids);
        analytics.setTotalItems(totalItems);
        analytics.setSuccessRate(successRate);
        analytics.setAverageBidPerItem(averageBidPerItem);
        analytics.setTopCategory("Vehicles"); // This should come from item-service
        analytics.setMonthlyComparison(comparison);

        return analytics;
    }

    private MonthlyComparison calculateMonthlyComparison(List<Bid> currentBids, List<Bid> prevBids) {
        // Calculate revenue for current month
        long currentRevenue = currentBids.stream()
            .collect(Collectors.toMap(
                Bid::getItemId,
                bid -> bid,
                (existing, replacement) -> existing.getAmount() > replacement.getAmount() ? existing : replacement
            ))
            .values()
            .stream()
            .mapToLong(Bid::getAmount)
            .sum();

        // Calculate revenue for previous month
        long prevRevenue = prevBids.stream()
            .collect(Collectors.toMap(
                Bid::getItemId,
                bid -> bid,
                (existing, replacement) -> existing.getAmount() > replacement.getAmount() ? existing : replacement
            ))
            .values()
            .stream()
            .mapToLong(Bid::getAmount)
            .sum();

        double revenueChange = prevRevenue > 0 ? ((currentRevenue - prevRevenue) / (double) prevRevenue) * 100 : 0;
        double profitChange = revenueChange; // Profit follows revenue percentage

        int currentAuctions = (int) currentBids.stream().map(Bid::getItemId).distinct().count();
        int prevAuctions = (int) prevBids.stream().map(Bid::getItemId).distinct().count();
        double auctionsChange = prevAuctions > 0 ? ((currentAuctions - prevAuctions) / (double) prevAuctions) * 100 : 0;

        int currentBidCount = currentBids.size();
        int prevBidCount = prevBids.size();
        double bidsChange = prevBidCount > 0 ? ((currentBidCount - prevBidCount) / (double) prevBidCount) * 100 : 0;

        return new MonthlyComparison(revenueChange, profitChange, auctionsChange, bidsChange);
    }

    private List<CategoryBreakdownDTO> calculateCategoryBreakdown(List<Bid> monthlyBids) {
        // For now, return mock data since we need item category information from item-service
        // TODO: Integrate with item-service to get actual category data

        Map<Integer, Bid> winningBids = monthlyBids.stream()
            .collect(Collectors.toMap(
                Bid::getItemId,
                bid -> bid,
                (existing, replacement) -> existing.getAmount() > replacement.getAmount() ? existing : replacement
            ));

        long totalRevenue = winningBids.values().stream().mapToLong(Bid::getAmount).sum();

        // Mock categories - replace with actual data from item-service
        List<CategoryBreakdownDTO> categories = new ArrayList<>();

        // For demonstration, divide items into categories
        long vehiclesRevenue = (long) (totalRevenue * 0.544);
        long jewelryRevenue = (long) (totalRevenue * 0.256);
        long generalRevenue = totalRevenue - vehiclesRevenue - jewelryRevenue;

        categories.add(new CategoryBreakdownDTO(
            "Vehicles",
            vehiclesRevenue,
            (long) (vehiclesRevenue * 0.25),
            (int) (winningBids.size() * 0.43),
            54.4
        ));

        categories.add(new CategoryBreakdownDTO(
            "Jewelry",
            jewelryRevenue,
            (long) (jewelryRevenue * 0.25),
            (int) (winningBids.size() * 0.25),
            25.6
        ));

        categories.add(new CategoryBreakdownDTO(
            "General",
            generalRevenue,
            (long) (generalRevenue * 0.26),
            (int) (winningBids.size() * 0.32),
            20.0
        ));

        return categories;
    }

    private List<WeeklyPerformanceDTO> calculateWeeklyPerformance(List<Bid> monthlyBids) {
        Map<Integer, List<Bid>> bidsByWeek = monthlyBids.stream()
            .collect(Collectors.groupingBy(bid -> {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                return bid.getBidTime().get(weekFields.weekOfMonth());
            }));

        List<WeeklyPerformanceDTO> weeklyData = new ArrayList<>();

        for (int week = 1; week <= 4; week++) {
            List<Bid> weekBids = bidsByWeek.getOrDefault(week, new ArrayList<>());

            Map<Integer, Bid> weekWinningBids = weekBids.stream()
                .collect(Collectors.toMap(
                    Bid::getItemId,
                    bid -> bid,
                    (existing, replacement) -> existing.getAmount() > replacement.getAmount() ? existing : replacement
                ));

            long weekRevenue = weekWinningBids.values().stream()
                .mapToLong(Bid::getAmount)
                .sum();

            int weekAuctions = weekWinningBids.size();

            weeklyData.add(new WeeklyPerformanceDTO("Week " + week, weekRevenue, weekAuctions));
        }

        return weeklyData;
    }

    private List<TopItemDTO> calculateTopItems(List<Bid> monthlyBids) {
        // Get winning bids grouped by item
        Map<Integer, Bid> winningBids = monthlyBids.stream()
            .collect(Collectors.toMap(
                Bid::getItemId,
                bid -> bid,
                (existing, replacement) -> existing.getAmount() > replacement.getAmount() ? existing : replacement
            ));

        // Count bids per item
        Map<Integer, Long> bidCounts = monthlyBids.stream()
            .collect(Collectors.groupingBy(Bid::getItemId, Collectors.counting()));

        // Create top items list
        return winningBids.values().stream()
            .sorted(Comparator.comparingLong(Bid::getAmount).reversed())
            .limit(5)
            .map(bid -> {
                int bidCount = bidCounts.getOrDefault(bid.getItemId(), 0L).intValue();
                // TODO: Fetch actual item name and category from item-service
                return new TopItemDTO(
                    "Item #" + bid.getItemId(),
                    bid.getAmount(),
                    bidCount,
                    "General" // Replace with actual category from item-service
                );
            })
            .collect(Collectors.toList());
    }
}
