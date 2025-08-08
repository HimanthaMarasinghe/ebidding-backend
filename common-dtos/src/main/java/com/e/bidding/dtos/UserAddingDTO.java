package com.e.bidding.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserAddingDTO {

    private String username;
    private String email;
    private String first_name;
    private String last_name;
    private String primary_phone;
    private String secondary_phone;
    private String role;
    private LocalDate date_of_birth;
    private String department;
    private LocationDTO location;
}
