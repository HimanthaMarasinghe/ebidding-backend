package com.e.bidding.item_service.model;

import com.e.bidding.item_service.common.ItemCategory;
import com.e.bidding.item_service.common.ItemCondition;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 💡 This enables auto-increment
    private Integer id;

    @Column(nullable = false)
    private String caseNumber;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private ItemCategory category;
    //TODO: Later should be changed into proper enum field in DB

    @Column(nullable = false)
    private int startingBid; //Convert into long if want

    @Column(nullable = false)
    private int increment;

    private int valuation;

    private ItemCondition condition;
    private String description;

    @ManyToOne(optional = true) // or true, if location is optional
    @JoinColumn(name = "location_id") // optional: sets the column name
    private Location location;

    @JsonManagedReference("auction-item")
    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL)
    private Auction auction;

    @JsonManagedReference("image-item")
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImage> images = new ArrayList<>();

    @JsonManagedReference("specs-item")
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<ItemSpecs> specs = new ArrayList<>();
}
