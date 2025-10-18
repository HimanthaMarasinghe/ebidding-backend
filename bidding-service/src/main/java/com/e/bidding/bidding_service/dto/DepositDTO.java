package com.e.bidding.bidding_service.dto;

import lombok.Data;

@Data
public class DepositDTO {
    private String userName;
    private long amount;

    public DepositDTO(String userName, long amount) {
        this.userName = userName;
        this.amount = amount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
