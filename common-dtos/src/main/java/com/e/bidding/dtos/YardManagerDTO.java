package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YardManagerDTO {
    private Integer id;
    private String username;
    private String email;
    private String primaryPhone;
    private String secondaryPhone;
    private String firstName;
    private String lastName;
    private Integer yardId;
}
