package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private Integer id;
    private String username;
    private String email;
    private String primaryPhone;
    private String secondaryPhone;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String role;
    private String userImageUrl;
    private String nicImageUrl;
}
