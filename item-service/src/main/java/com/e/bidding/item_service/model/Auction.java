package com.e.bidding.item_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Auction {
    @Id
    private Integer id;

    private LocalDateTime startingTime;
    private LocalDateTime endingTime;


    @OneToOne
    @MapsId  // Important: tells JPA to use this ID as the FK from Item
    @JoinColumn(name = "id") // FK column (same as PK)
    private Item item;
}
