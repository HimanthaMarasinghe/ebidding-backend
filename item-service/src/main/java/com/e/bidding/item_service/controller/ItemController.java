package com.e.bidding.item_service.controller;

import com.e.bidding.item_service.dto.FavoriteDTO;
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





    @PostMapping("/createBundle")
    public ResponseDTO<List<ItemDTO>> createBundle(@RequestBody List<ItemDTO> itemDTOArray) {
        return itemService.saveBulk(itemDTOArray);
    }

    @GetMapping("/searchItem/{term}")
    public List<ItemDTO> searchByTerm(@PathVariable String term) {
        return itemService.findByTerm(term);
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
}
