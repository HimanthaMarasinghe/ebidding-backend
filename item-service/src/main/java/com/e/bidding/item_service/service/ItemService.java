package com.e.bidding.item_service.service;

import com.e.bidding.item_service.dto.ItemDTO;
import com.e.bidding.item_service.dto.ItemDocDTO;
import com.e.bidding.item_service.dto.ItemImageDTO;
import com.e.bidding.dtos.ResponseDTO;
import com.e.bidding.item_service.model.*;
import com.e.bidding.item_service.projection.ItemToScheduleProjection;
import com.e.bidding.item_service.repo.ItemDocRepo;
import com.e.bidding.item_service.repo.ItemImageRepo;
import com.e.bidding.item_service.repo.ItemRepo;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepo itemRepo;
    private final ItemImageRepo itemImageRepo;
    private final ItemDocRepo itemDocRepo;
    private final ModelMapper modelMapper;

    public ItemService(ItemRepo itemRepo, ItemImageRepo itemImageRepo, ItemDocRepo itemDocRepo, ModelMapper modelMapper) {
        this.itemRepo = itemRepo;
        this.itemImageRepo = itemImageRepo;
        this.itemDocRepo = itemDocRepo;
        this.modelMapper = modelMapper;
    }

    public List<ItemDTO> findAll() {
        List<Item> items = itemRepo.findAll();
        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
    }

    public List<ItemDTO> findNotScheduled() {
        List<Item> items = itemRepo.findItemsWithNoAuction();
        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
    }

    public List<ItemDTO> findPending() {
        List<Item> items = itemRepo.findPendingItems(LocalDateTime.now(ZoneOffset.UTC));
        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
    }

    public List<ItemDTO> findActive() {
        List<Item> items = itemRepo.findActiveItems(LocalDateTime.now(ZoneOffset.UTC));
        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
    }

    public List<ItemDTO> findComplete() {
        List<Item> items = itemRepo.findCompleteItems(LocalDateTime.now(ZoneOffset.UTC));
        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
    }

    public ItemDTO findById(Integer id) {
        ItemDTO item = modelMapper.map(itemRepo.findById(id), ItemDTO.class);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        if (item.getAuction() == null)
            item.setStatus("Not Scheduled");
        else if (item.getAuction().getStartingTime() != null && now.isBefore(item.getAuction().getStartingTime())) {
            Duration timeToStart = Duration.between(now, item.getAuction().getStartingTime());
            if(timeToStart.toMinutes() <= 60){
                item.setStatus("Starting Soon");
            }else{
                item.setStatus("Pending");
            }
        } else if (item.getAuction().getEndingTime() != null && now.isBefore(item.getAuction().getEndingTime())) {
            Duration timeLeft = Duration.between(now, item.getAuction().getEndingTime());
            if (timeLeft.toMinutes() <= 60) {
                item.setStatus("Ending Soon");
            } else {
                item.setStatus("Active"); // Assuming "Active" when the auction is ongoing
            }
        } else {
            item.setStatus("Completed");
        }
        return item;
    }

    public List<ItemToScheduleProjection> itemsToSchedule(List<Integer> ids) {
        return itemRepo.itemsToSchedule(ids);
    }

    /**
     * Save one Item, with or without auction details
     * @param itemDTO ItemDTO that should be saved
     * @return A response object that include the success status, saved data and a message
     */
    @Transactional
    public ResponseDTO<Integer> save(
            ItemDTO itemDTO,
            List<MultipartFile> images,
            MultipartFile cover,
            List<MultipartFile> files
            ) throws IOException {
        Item newItem = modelMapper.map(itemDTO, Item.class);
        Auction auction = newItem.getAuction();
        if (auction != null && auction.getStartingTime() != null && auction.getEndingTime() != null) {
            newItem.getAuction().setItem(newItem);
        } else {
            newItem.setAuction(null);
        }

        if (newItem.getSpecs() != null && !newItem.getSpecs().isEmpty()) {
            for (ItemSpecs spec : newItem.getSpecs()) {
                spec.setItem(newItem);
            }
        } else {
            newItem.setSpecs(null);
        }

        Item savedItem = itemRepo.save(newItem);
        ItemDTO savedItemDTO = modelMapper.map(savedItem, ItemDTO.class);

        // Set your shared folder path
        String basePath = "D:\\EBidingFiles\\items\\" + savedItemDTO.getCaseNumber() + "-" + savedItemDTO.getId();
        Path imageFolder = Paths.get(basePath + "\\images");
        Path docFolder = Paths.get(basePath + "\\docs");

        if (!Files.exists(imageFolder)) Files.createDirectories(imageFolder);
        if (!Files.exists(docFolder)) Files.createDirectories(docFolder);

        List<ItemImageDTO> imageDTOList = new ArrayList<>();

        if (cover != null) {
            String covername = savedItemDTO.getCaseNumber() + "_0_" + cover.getOriginalFilename();
            Path coverPath = Paths.get(imageFolder.toString(), covername);
            long coverBytes = Files.copy(cover.getInputStream(), coverPath, StandardCopyOption.REPLACE_EXISTING);
            if(coverBytes > 0) {
                ItemImageDTO itemImageDTO = new ItemImageDTO();
                itemImageDTO.setItemId(savedItemDTO.getId());
                itemImageDTO.setCover(true);
                itemImageDTO.setUrl(covername);
                imageDTOList.add(itemImageDTO);
            }
        }


        if (images != null && !images.isEmpty()) {
            // Now 'images' contains multiple files
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                String filename = savedItemDTO.getCaseNumber() + "_" + (i+1) + "_" + image.getOriginalFilename();
                Path filePath = imageFolder.resolve(filename);
                long copyBytes = Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                if(copyBytes > 0) {
                    ItemImageDTO itemImageDTO = new ItemImageDTO();
                    itemImageDTO.setItemId(savedItemDTO.getId());
                    itemImageDTO.setUrl(filename);
                    imageDTOList.add(itemImageDTO);
                }
            }
        }

        if (!imageDTOList.isEmpty()) {
            List<ItemImage> imageEntities = imageDTOList.stream()
                    .map(dto -> {
                        ItemImage entity = new ItemImage();
                        entity.setItem(savedItem);
                        entity.setUrl(dto.getUrl());
                        entity.setCover(dto.getCover());
                        return entity;
                    })
                    .collect(Collectors.toList());

            itemImageRepo.saveAll(imageEntities);
        }

        List<ItemDocDTO> docDTOList = new ArrayList<>();

        if(files != null && !files.isEmpty()) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile doc = files.get(i);
                String filename = savedItemDTO.getCaseNumber() + "_" + i + "_" + doc.getOriginalFilename();
                Path filePath = docFolder.resolve(filename);
                long copyBytes = Files.copy(doc.getInputStream(), filePath);

                if(copyBytes > 0) {
                    ItemDocDTO itemDocDTO = new ItemDocDTO();
                    itemDocDTO.setItemId(savedItemDTO.getId());
                    itemDocDTO.setUrl(filename);
                    docDTOList.add(itemDocDTO);
                }
            }
        }

        if (!docDTOList.isEmpty()) {
            List<ItemDoc> docEntities = docDTOList.stream()
                    .map(dto -> {
                        ItemDoc entity = new ItemDoc();
                        entity.setItem(savedItem);
                        entity.setUrl(dto.getUrl());
                        return entity;
                    })
                    .collect(Collectors.toList());

            itemDocRepo.saveAll(docEntities);
        }

        return new ResponseDTO<>(true, savedItemDTO.getId(), "Item saved successfully");
    }

    /**
     * Save a list ot items at once.
     * @param itemDTOs List of items that should be saved
     * @return List of items that got saved
     */
    @Transactional
    public ResponseDTO<List<ItemDTO>> saveBulk(List<ItemDTO> itemDTOs) {
        List<Item> items = itemDTOs.stream()
                .map(dto -> {
                    Item item = modelMapper.map(dto, Item.class);
                    if (item.getAuction() != null) {
                        item.getAuction().setItem(item);
                    }
                    return item;
                })
                .collect(Collectors.toList());

        List<Item> savedItems = itemRepo.saveAll(items);

        List<ItemDTO> savedItemDTOList = savedItems.stream()
                .map(item -> modelMapper.map(item, ItemDTO.class))
                .collect(Collectors.toList());

        return new ResponseDTO<>(true, savedItemDTOList, "Items saved successfully");
    }

    public List<ItemDTO> findByTerm(String term) {
        List<Item> filteredItems = itemRepo.searchByTerm(term);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        return filteredItems.stream()
                .map(item -> {
                    ItemDTO i =  modelMapper.map(item, ItemDTO.class);
                            if (i.getAuction() == null)
                                i.setStatus("Not Scheduled");
                            else if (i.getAuction().getStartingTime() != null && now.isBefore(item.getAuction().getStartingTime())) {
                                Duration timeToStart = Duration.between(now, i.getAuction().getStartingTime());
                                if(timeToStart.toMinutes() <= 60){
                                    i.setStatus("Starting Soon");
                                }else{
                                    i.setStatus("Pending");
                                }
                            } else if (i.getAuction().getEndingTime() != null && now.isBefore(item.getAuction().getEndingTime())) {
                                Duration timeLeft = Duration.between(now, i.getAuction().getEndingTime());
                                if (timeLeft.toMinutes() <= 60) {
                                    i.setStatus("Ending Soon");
                                } else {
                                    i.setStatus("Active"); // Assuming "Active" when the auction is ongoing
                                }
                            } else {
                                i.setStatus("Completed");
                            }
                    return i;
                }

                )
                .collect(Collectors.toList());
    }

}
