package com.e.bidding.user_service.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "bidder")
@Getter
public class Bidder extends UserProfile {

    private String user_image_url;
    private String nic_image_url;

    public String getUser_image_url() {
        return user_image_url;
    }

    public void setUser_image_url(String user_image_url) {
        this.user_image_url = user_image_url;
    }

    public String getNic_image_url() {
        return nic_image_url;
    }

    public void setNic_image_url(String nic_image_url) {
        this.nic_image_url = nic_image_url;
    }
}

