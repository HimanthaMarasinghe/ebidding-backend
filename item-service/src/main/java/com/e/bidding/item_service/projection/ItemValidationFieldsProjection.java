package com.e.bidding.item_service.projection;

import java.time.LocalDateTime;

public interface ItemValidationFieldsProjection {
    int getStartingBid();
    int getIncrement();
    AuctionView getAuction();

    interface AuctionView {
        LocalDateTime getStartingTime();
        LocalDateTime getEndingTime();
    }
}