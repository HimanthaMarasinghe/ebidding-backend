package com.e.bidding.user_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "yard_manager")
public class YardManager extends UserProfile {
    private Integer yard_id;
}