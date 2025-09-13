package com.e.bidding.item_service.dto;

import com.e.bidding.item_service.model.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetItemsResponseDTO {
    private List<ItemDTO> content;
    private boolean hasNext;
    private int currentPage;
}
