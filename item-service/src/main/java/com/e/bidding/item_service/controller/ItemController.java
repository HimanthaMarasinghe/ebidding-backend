package com.e.bidding.item_service.controller;

import com.e.bidding.item_service.common.ItemCategory;
import com.e.bidding.item_service.common.ItemState;
import com.e.bidding.item_service.dto.ItemDTO;
import com.e.bidding.dtos.ResponseDTO;
import com.e.bidding.item_service.projection.ItemToScheduleProjection;
import com.e.bidding.item_service.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/is/v1")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

//    /**Depreciated*/
//    @GetMapping("/allItems")
//    public List<ItemDTO> allItems() {
//        return itemService.findAll();
//    }
//
//    /**Depreciated - (Use getItem)*/
//    @GetMapping("/getItemsNotScheduled")
//    public List<ItemDTO> getItemsNotScheduled(@RequestParam(required = false) String category) {
//        return itemService.findNotScheduled();
//    }
//
//    /**Depreciated - (Use getItem)*/
//    @GetMapping("/getItemsPending")
//    public List<ItemDTO> getItemsPending(@RequestParam(required = false) String category) {
//        return itemService.findPending();
//    }
//
//    /**Depreciated - (Use getItem)*/
//    @GetMapping("/getItemsActive")
//    public List<ItemDTO> getItemsActive(@RequestParam(required = false) String category) {
//        return itemService.findActive();
//    }
//
//    /**Depreciated - (Use getItem)*/
//    @GetMapping("/getItemsComplete")
//    public List<ItemDTO> getItemsComplete(@RequestParam(required = false) String category) {
//        return itemService.findComplete();
//    }

    @PostMapping ("/itemsToSchedule")
    public List<ItemToScheduleProjection> itemsToSchedule(@RequestBody List<Integer> ids) {
        return itemService.itemsToSchedule(ids);
    }

    @GetMapping("/getItem/{id}")
    public ItemDTO getItem(@PathVariable Integer id) {
        return itemService.findById(id);
    }

    @PostMapping(value = "/createItem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDTO<Integer> createItem(
            @RequestPart("item") String itemJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "cover", required = false) MultipartFile cover
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ItemDTO itemDTO = mapper.readValue(itemJson, ItemDTO.class);

        return itemService.save(itemDTO, images, cover, files);
    }




//    /**Depreciated*/
//    @PostMapping("/createBundle")
//    public ResponseDTO<List<ItemDTO>> createBundle(@RequestBody List<ItemDTO> itemDTOArray) {
//        return itemService.saveBulk(itemDTOArray);
//    }
//
//    /**Depreciated - (Use getItem)*/
//    @GetMapping("/searchItem/{term}")
//    public List<ItemDTO> searchByTerm(@PathVariable String term) {
//        return itemService.findByTerm(term);
//    }

    @GetMapping("/getItems")
    public List<ItemDTO> getItems(
        @RequestParam(required = false) ItemState status,
        @RequestParam(required = false) String searchTerm,
        @RequestParam(required = false) ItemCategory category,
        @RequestParam(required = false) String orderBy,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) String direction)
    {
        if (status == null) {
            System.out.println("Status is invalid");
            return null;
        } else if (searchTerm == null || searchTerm.isEmpty()) {
            return itemService.findItems(status, category, orderBy, limit, page, direction);
        } else {
            return itemService.search(searchTerm, status, category, limit, page);
        }
    }
}