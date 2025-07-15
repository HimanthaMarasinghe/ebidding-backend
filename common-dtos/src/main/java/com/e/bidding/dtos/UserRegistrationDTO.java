package com.e.bidding.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class  UserRegistrationDTO {

    private String username;
    private String password;
    private String email;
    private String first_name;
    private String last_name;
    private String primary_phone;
    private String secondary_phone;
    private LocalDate date_of_birth;
    private String user_image_url;
    private String nic_image_url;
}

