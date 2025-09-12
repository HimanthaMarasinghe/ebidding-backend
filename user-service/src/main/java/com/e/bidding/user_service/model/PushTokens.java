package com.e.bidding.user_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Optional;

@Entity
@Data
@Table(name = "push_tokens")
public class PushTokens {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column (nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

}

