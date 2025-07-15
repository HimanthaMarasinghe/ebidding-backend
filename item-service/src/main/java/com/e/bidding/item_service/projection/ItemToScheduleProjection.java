package com.e.bidding.item_service.projection;

public interface ItemToScheduleProjection {
    Integer getId();
    String getCaseNumber();
    String getTitle();
    Double getStartingBid();
    Double getIncrement();
}
