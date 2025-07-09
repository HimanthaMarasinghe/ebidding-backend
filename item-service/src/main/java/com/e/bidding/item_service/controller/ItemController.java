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

    @GetMapping("/getItemsNotScheduled")
    public List<ItemDTO> getItemsNotScheduled() {
        return itemService.findNotScheduled();
    }

    @GetMapping("/getItemsPending")
    public List<ItemDTO> getItemsPending() {
        return itemService.findPending();
    }

    @GetMapping("/getItemsActive")
    public List<ItemDTO> getItemsActive() {
        return itemService.findActive();
    }

    @GetMapping("/getItemsComplete")
    public List<ItemDTO> getItemsComplete() {
        return itemService.findComplete();
    }

    @GetMapping("/getItem/{id}")
    public ItemDTO getItem(@PathVariable Integer id) {
        return itemService.findById(id);
    }

    @PostMapping("/createItem")
    public ItemDTO createItem(@RequestBody ItemDTO itemDTO) {
        return itemService.save(itemDTO);
    }

    //This method is only to be used to store data in to DB while testing. Not in the application.
    @PostMapping("/createBundle")
    public List<ItemDTO> createBundle(@RequestBody List<ItemDTO> itemDTOArray) {
        return itemService.saveBulk(itemDTOArray);
    }
}
