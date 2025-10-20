package com.e.bidding.item_service.controller;

import com.e.bidding.item_service.common.ItemCategory;
import com.e.bidding.item_service.common.ItemState;
import com.e.bidding.item_service.dto.FavoriteDTO;
import com.e.bidding.item_service.dto.GetItemsResponseDTO;
import com.e.bidding.item_service.dto.ItemDTO;
import com.e.bidding.dtos.ResponseDTO;
import com.e.bidding.item_service.projection.ItemToScheduleProjection;
import com.e.bidding.item_service.service.AuctionService;
import com.e.bidding.item_service.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/is/v1")
public class ItemController {

    private final ItemService itemService;
    private final AuctionService auctionService;

    public ItemController(ItemService itemService, AuctionService auctionService) {
        this.itemService = itemService;
        this.auctionService = auctionService;
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
    public GetItemsResponseDTO getItems(
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

    @PostMapping("/addFavorite")
    public ResponseDTO<Integer> addFavorite(@RequestBody FavoriteDTO favoriteDTO){
        try {
            return itemService.addFavorite(favoriteDTO);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseDTO<>(false, null, "Error adding favorite: " + e.getMessage());
        }
    }

    @GetMapping("/getFavorite/{userId}")
    public List<ItemDTO> getFavorite(@PathVariable Integer userId){
        return itemService.findFavorite(userId);
    }

    @GetMapping("/getActiveItemsByIDs/{itemIds}")
    public ResponseEntity<List<ItemDTO>> getActiveItemsByIDs(@PathVariable List<Integer> itemIds){
        if(itemIds.isEmpty()){
            log.error("NO ITEM IDs FOUND");
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(itemService.getActiveItemsById(itemIds));
    }

    @GetMapping("/getItemsByIDs/{itemIds}")
    public ResponseEntity<List<ItemDTO>> getItemsByIDs(@PathVariable List<Integer> itemIds){
        if(itemIds.isEmpty()){
            log.error("NO ITEM IDs FOUND FOR THIS");
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(itemService.getEndedItemsById(itemIds));
    }

    @GetMapping("/getMyYardItems/{username}")
    public ResponseEntity<List<ItemDTO>> getYardItems(@PathVariable String username){
        if(username.isEmpty()){
            log.error("User name not found");
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(auctionService.getYardItems(username));
    }
}
