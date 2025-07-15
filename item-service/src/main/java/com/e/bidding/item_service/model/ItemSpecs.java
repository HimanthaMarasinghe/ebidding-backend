package com.e.bidding.item_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ItemSpecs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 💡 This enables auto-increment
    private Integer id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    @JsonBackReference("specs-item")
    private Item item;
}
