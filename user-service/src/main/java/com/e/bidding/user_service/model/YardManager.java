package com.e.bidding.user_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "yard_manager")
public class YardManager extends UserProfile {
    private String yard_name;
    private String license_number;

    public String getYard_name() {
        return yard_name;
    }

    public void setYard_name(String yard_name) {
        this.yard_name = yard_name;
    }

    public String getLicense_number() {
        return license_number;
    }

    public void setLicense_number(String license_number) {
        this.license_number = license_number;
    }
}