package com.e.bidding.item_service.dto;

import com.e.bidding.item_service.model.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemImageDTO {
    private Integer id;
    private String url;
    private Boolean cover = false;
    private Integer itemId;
}
