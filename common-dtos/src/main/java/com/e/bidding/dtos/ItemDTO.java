package com.e.bidding.dtos;

import com.e.bidding.dtos.Enums.ItemCategory;
import com.e.bidding.dtos.Enums.ItemCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private List<ItemSpecsDTO> specs;
    private AuctionDTO auction;
    private List<ItemImageDTO> images;
    private List<ItemDocDTO> docs;

    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        if (auction == null) {
            this.status = "Not Scheduled";
        } else if (auction.getStartingTime() != null && now.isBefore(auction.getStartingTime())) {
            Duration timeToStart = Duration.between(now, auction.getStartingTime());
            if (timeToStart.toMinutes() <= 60) {
                this.status = "Starting Soon";
            } else {
                this.status = "Pending";
            }
        } else if (auction.getEndingTime() != null && now.isBefore(auction.getEndingTime())) {
            Duration timeLeft = Duration.between(now, auction.getEndingTime());
            if (timeLeft.toMinutes() <= 60) {
                this.status = "Ending Soon";
            } else {
                this.status = "Active";
            }
        } else {
            this.status = "Completed";
        }
    }
}
