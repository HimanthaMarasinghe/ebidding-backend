package com.e.bidding.item_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItmeSpecsDTO {
    private Integer id;
    private String key;
    private String value;
    private Integer itemId;
}
