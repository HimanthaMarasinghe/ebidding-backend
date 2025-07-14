package com.e.bidding.item_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDTO {
    private String id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
}
