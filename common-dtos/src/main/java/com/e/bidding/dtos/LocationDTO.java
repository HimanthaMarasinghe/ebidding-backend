package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDTO {
    private Integer id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
}
