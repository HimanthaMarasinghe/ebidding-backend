package com.e.bidding.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemSpecsDTO {
    private Integer id;
    private String key;
    private String value;
    private Integer itemId;
}