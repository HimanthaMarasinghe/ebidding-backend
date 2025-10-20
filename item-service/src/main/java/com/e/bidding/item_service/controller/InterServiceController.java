package com.e.bidding.item_service.controller;

import com.e.bidding.dtos.ActiveItemBidValidationDTO;
import com.e.bidding.item_service.service.ItemService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/is/v1")
public class InterServiceController {

    private final ItemService itemService;

    public InterServiceController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/getItemValidationFields/{itemId}")
    public ActiveItemBidValidationDTO getItemValidationFields(@PathVariable Integer itemId) {
        return itemService.getItemValidationFields(itemId);
    }

}
