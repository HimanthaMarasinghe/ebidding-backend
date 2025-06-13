package com.e.bidding.item_service.controller;

import com.e.bidding.item_service.dto.ItemDTO;
import com.e.bidding.item_service.service.ItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/is/v1")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/allItems")
    public List<ItemDTO> allItems() {
        return itemService.findAll();
    }

    @GetMapping("/getItem/{id}")
    public ItemDTO getItem(@PathVariable Integer id) {
        return itemService.findById(id);
    }

    @PostMapping("/createItem")
    public ItemDTO createItem(@RequestBody ItemDTO itemDTO) {
        return itemService.save(itemDTO);
    }
}
