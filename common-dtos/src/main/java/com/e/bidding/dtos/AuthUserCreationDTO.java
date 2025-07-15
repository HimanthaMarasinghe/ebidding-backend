package com.e.bidding.dtos;

import lombok.Data;

@Data
public class AuthUserCreationDTO {

    private String username;
    private String email;
    private String primary_phone;
    private String role;
}
