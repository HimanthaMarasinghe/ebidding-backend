package com.e.bidding.user_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "auction_manager")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AuctionManager extends UserProfile {
    private String auction_center;
    private String designation;

    public String getAuction_center() {
        return auction_center;
    }

    public void setAuction_center(String auction_center) {
        this.auction_center = auction_center;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
