package com.e.bidding.bidding_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "DEPOSIT")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEPOSIT_ID")
    private Integer depositId;

    @Column(name = "USER_NAME", length = 100)
    private String userName;

    @Column(name = "AMOUNT", precision = 15)
    private long amount;

    @Column(name = "BID_TIME")
    private LocalDateTime bidTime;
}
