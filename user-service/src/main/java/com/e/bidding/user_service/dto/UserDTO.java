package com.e.bidding.user_service.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor


public class UserDTO {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String primaryPhone;
    private LocalDate date_of_birth;

}
