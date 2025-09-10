package com.e.bidding.item_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteDTO {
    private Integer id;
    private Integer userId;
    private Integer itemId;
}
