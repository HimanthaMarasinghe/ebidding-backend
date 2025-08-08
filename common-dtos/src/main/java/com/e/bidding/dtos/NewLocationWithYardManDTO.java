package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewLocationWithYardManDTO {
    private Integer id;
    private LocationDTO location;
    private boolean success;
}
