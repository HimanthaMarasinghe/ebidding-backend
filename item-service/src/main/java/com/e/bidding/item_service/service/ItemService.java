package com.e.bidding.item_service.service;

import com.e.bidding.dtos.ActiveItemBidValidationDTO;
import com.e.bidding.dtos.AuctionScheduleEventDTO;
import com.e.bidding.item_service.common.ItemCategory;
import com.e.bidding.item_service.common.ItemState;
import com.e.bidding.item_service.dto.*;
import com.e.bidding.item_service.kafka.NewAuctionScheduleProducer;
import com.e.bidding.item_service.projection.ItemValidationFieldsProjection;
import com.e.bidding.item_service.repo.ItemCustomRepository;
import com.e.bidding.item_service.repo.FavoriteRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.e.bidding.dtos.ResponseDTO;
import com.e.bidding.item_service.model.*;
import com.e.bidding.item_service.projection.ItemToScheduleProjection;
import com.e.bidding.item_service.repo.ItemDocRepo;
import com.e.bidding.item_service.repo.ItemImageRepo;
import com.e.bidding.item_service.repo.ItemRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepo itemRepo;
    private final ItemImageRepo itemImageRepo;
    private final ItemDocRepo itemDocRepo;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final ItemCustomRepository itemCustomRepository;
    private final View error;
    private final FavoriteRepo favoriteRepo;
    private final NewAuctionScheduleProducer newAuctionScheduleProducer;

    public ItemService(ItemRepo itemRepo, ItemImageRepo itemImageRepo, ItemDocRepo itemDocRepo, FavoriteRepo favoriteRepo, ModelMapper modelMapper, ObjectMapper objectMapper, StringRedisTemplate redisTemplate, ItemCustomRepository itemCustomRepository, View error, NewAuctionScheduleProducer newAuctionScheduleProducer) {

        this.itemRepo = itemRepo;
        this.itemImageRepo = itemImageRepo;
        this.itemDocRepo = itemDocRepo;
        this.favoriteRepo = favoriteRepo;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.itemCustomRepository = itemCustomRepository;
        this.error = error;
        this.newAuctionScheduleProducer = newAuctionScheduleProducer;
    }

//    /**Depreciated*/
//    public List<ItemDTO> findAll() {
//        List<Item> items = itemRepo.findAll();
//        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
//    }
//
//    /**Depreciated - (Use findItems)*/
//    public List<ItemDTO> findNotScheduled() {
//        List<Item> items = itemRepo.findItemsWithNoAuction();
//        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
//    }
//
//    /**Depreciated - (Use findItems)*/
//    public List<ItemDTO> findPending() {
//        List<Item> items = itemRepo.findPendingItems(LocalDateTime.now(ZoneOffset.UTC));
//        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
//    }
//
//    /**Depreciated - (Use findItems)*/
//    public List<ItemDTO> findActive() {
//        List<Item> items = itemRepo.findActiveItems(LocalDateTime.now(ZoneOffset.UTC));
//        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
//    }
//    /**Depreciated - (Use findItems)*/
//    public List<ItemDTO> findComplete() {
//        List<Item> items = itemRepo.findCompleteItems(LocalDateTime.now(ZoneOffset.UTC));
//        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
//    }

    /**
     * Create a Pageble object based on the input, or return the default on foe the state
     * @param state - state of the item (Required)
     * @param page - page / default 0 (Not Required)
     * @param limit - limit / default 12 (Not Required)
     * @param orderBy - which field should be used to order the result / default is based on the state (Not Required)
     * @param direction - direction { ASC | DESC } / default ASC (Not Required)
     */
    private Pageable createPageable(ItemState state, Integer page, Integer limit, String orderBy, String direction) {
        // ✅ set defaults if null
        int pageNum = (page == null || page < 0) ? 0 : page;
        int pageSize = (limit == null || limit <= 0) ? 24 : limit;

        // ✅ decide default sorting based on state
        String defaultSortField;
        Sort.Direction defaultSortDirection;

        if (state == null) {
            state = ItemState.NotScheduled; // default state
        }

        switch (state) {
            case Pending -> {
                defaultSortField = "a.startingTime";
                defaultSortDirection = Sort.Direction.ASC;
            }
            case Active -> {
                defaultSortField = "a.endingTime";
                defaultSortDirection = Sort.Direction.ASC;
            }
            case Completed -> {
                defaultSortField = "a.endingTime";
                defaultSortDirection = Sort.Direction.DESC;
            }
            default -> {
                defaultSortField = "i.id";
                defaultSortDirection = Sort.Direction.ASC;
            }
        }

        // ✅ if client provided orderBy/direction, override defaults
        String sortField = (orderBy != null && !orderBy.isEmpty()) ? orderBy : defaultSortField;
        Sort.Direction sortDirection = direction != null ?
                    direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC
                    : defaultSortDirection;

        return PageRequest.of(pageNum, pageSize, Sort.by(sortDirection, sortField));
    }

    /**Find Items with no search*/
    public GetItemsResponseDTO findItems(
            ItemState state,
            ItemCategory category,
            String orderBy,
            Integer limit,
            Integer page,
            String direction
    ) {
        try {
            Pageable pageable = createPageable(state, page, limit, orderBy, direction);

            Slice<Item> slice;

            if (category == null) {
                slice = switch (state) {
                    case NotScheduled -> itemRepo.findItemsWithNoAuction(pageable);
                    case Pending -> itemRepo.findPendingItems(LocalDateTime.now(ZoneOffset.UTC), pageable);
                    case Active -> itemRepo.findActiveItems(LocalDateTime.now(ZoneOffset.UTC), pageable);
                    case Completed -> itemRepo.findCompleteItems(LocalDateTime.now(ZoneOffset.UTC), pageable);
                };
            } else {
                slice = switch (state) {
                    case NotScheduled -> itemRepo.filterItemsWithNoAuction(category, pageable);
                    case Pending -> itemRepo.filterPendingItems(LocalDateTime.now(ZoneOffset.UTC), category, pageable);
                    case Active -> itemRepo.filterActiveItems(LocalDateTime.now(ZoneOffset.UTC), category, pageable);
                    case Completed -> itemRepo.filterCompleteItems(LocalDateTime.now(ZoneOffset.UTC), category, pageable);
                };
            }

            // Map content to DTOs
            List<ItemDTO> dtos = modelMapper.map(slice.getContent(), new TypeToken<List<ItemDTO>>() {}.getType());
            dtos.forEach(ItemDTO::updateStatus);
            // Build response DTO
            return new GetItemsResponseDTO(dtos, slice.hasNext(), slice.getNumber() + 1);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public ItemDTO findById(Integer id) {
        // 1. Try Redis
        String key = "item:" + id;
        ItemDTO item = null;
        String cachedItemJson = null;

        try {
            BoundValueOperations<String, String> ops = redisTemplate.boundValueOps(key);
            cachedItemJson = ops.getAndExpire(java.time.Duration.ofMinutes(10));
        } catch (Exception e) {
            logger.warn("Failed to get item from Redis", e);
        }

        if (cachedItemJson != null) {
            try {
                item = objectMapper.readValue(cachedItemJson, ItemDTO.class);
            } catch (JsonProcessingException e) {
                logger.warn("Error parsing cached item", e);
            }
        }

        // 2. Lord from DB and set in redis
        if (item == null) {
            // Load from DB
            item = modelMapper.map(itemRepo.findById(id), ItemDTO.class);
            if(item.getAuction() != null) { //Only save in redis if it is not "Not Scheduled"
                // Set in redis
                try {
                    String json = objectMapper.writeValueAsString(item);
                    redisTemplate.opsForValue().set(key, json, java.time.Duration.ofMinutes(10));
                } catch (Exception e) {
                    logger.warn("Failed to cache item in Redis", e);
                }
            }
        }

        // 3. Always update status with current time
        item.updateStatus();
        if(item.getStatus().equals("Active") || item.getStatus().equals("Ending Soon")) {
          // Set validation fields for bidding service.
            try {
                String activeItemKey = "activeItem:" + item.getId();
                ActiveItemBidValidationDTO activeItemDTO = new ActiveItemBidValidationDTO(
                        item.getStartingBid(),
                        item.getIncrement(),
                        item.getAuction().getStartingTime(),
                        item.getAuction().getEndingTime());
                String activeJson = objectMapper.writeValueAsString(activeItemDTO);
                redisTemplate.opsForValue().set(activeItemKey, activeJson, Duration.ofMinutes(10));
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
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

        //publishing auction schedule info
        if(savedItemDTO.getId()!=null && auction != null && auction.getStartingTime() != null && auction.getEndingTime() != null){
            AuctionScheduleEventDTO auctionScheduleEvent=new AuctionScheduleEventDTO(savedItemDTO.getId(),auction.getEndingTime());
            logger.info(auction.getEndingTime().toString());
            newAuctionScheduleProducer.SendMessage(auctionScheduleEvent);
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

//    /**Depreciated - (Use search)*/
//    public List<ItemDTO> findByTerm(String term) {
//        List<Item> filteredItems = itemRepo.searchByTerm(term);
//        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
//
//        return filteredItems.stream()
//                .map(item -> {
//                    ItemDTO i =  modelMapper.map(item, ItemDTO.class);
//                    i.updateStatus();
//                    return i;
//                }
//
//                )
//                .collect(Collectors.toList());
//    }

    public GetItemsResponseDTO search(
            String term,
            ItemState status,
            ItemCategory category,
            Integer limit,
            Integer page
    ) {
        if (limit == null || limit < 0) limit = 24;
        if (page == null || page < 0) page = 0;
        List<Item> result = itemCustomRepository.searchItems(term, status, category, limit, page);

        boolean hasNext = false;

        // If we got more than 'limit', then there is a next page
        if (result.size() > limit) {
            hasNext = true;
            result = result.subList(0, limit); // remove the extra item
        }

        // Map to DTOs
        List<ItemDTO> dtos = modelMapper.map(result, new TypeToken<List<ItemDTO>>() {}.getType());
        dtos.forEach(ItemDTO::updateStatus);

        return new GetItemsResponseDTO(dtos, hasNext, page != null ? page + 1 : 1);
    }


    public ResponseDTO<Integer> addFavorite(FavoriteDTO favoriteDTO) {
        try {
            Favorite favorite = modelMapper.map(favoriteDTO, Favorite.class);
            Favorite savedFavorite = favoriteRepo.save(favorite);
            logger.info("Favorite added successfully");
            return new ResponseDTO<>(true, savedFavorite.getId(), "Favorite added successfully");
        } catch (Exception e) {
            logger.error("Error adding favorite: ", e);
            return new ResponseDTO<>(false, null, "Error adding favorite: " + e.getMessage());

        }
    }

    public List<ItemDTO> findFavorite(Integer userId) {
        try {
            // 1. Find all favorites for the user
            List<Favorite> favorites = favoriteRepo.findAllByUserId(userId);

            // 2. Get corresponding items
            List<Item> items = favorites.stream()
                    .map(fav -> itemRepo.findById(fav.getItemId()).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

            // 3. Convert items to ItemDTO
            return items.stream()
                    .map(item -> modelMapper.map(item, ItemDTO.class))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error fetching favorites for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }

    }

    public List<ItemDTO> getActiveItemsById(List<Integer> itemIds) {
        if(itemIds.isEmpty()){
            return Collections.emptyList();
        }
        List <Item> items=itemRepo.findAllById(itemIds);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        List<Item> activeItems = items.stream()
                .filter(item -> item.getAuction() != null
                        && item.getAuction().getStartingTime() != null
                        && item.getAuction().getEndingTime() != null
                        && now.isAfter(item.getAuction().getStartingTime())
                        && now.isBefore(item.getAuction().getEndingTime()))
                .toList();
        List<ItemDTO> activeItemDTOs = modelMapper.map(activeItems, new TypeToken<List<ItemDTO>>() {}.getType());
        activeItemDTOs.forEach(ItemDTO::updateStatus);
        return activeItemDTOs;
    }

    public ActiveItemBidValidationDTO getItemValidationFields(Integer itemId) {
        ItemValidationFieldsProjection i = itemRepo.findProjectedById(itemId);
        String activeItemKey = "activeItem:" + itemId;
        try {
            ActiveItemBidValidationDTO activeItemDTO = new ActiveItemBidValidationDTO(
                    i.getStartingBid(),
                    i.getIncrement(),
                    i.getAuction().getStartingTime(),
                    i.getAuction().getEndingTime()
            );
            String activeJson = objectMapper.writeValueAsString(activeItemDTO);
            redisTemplate.opsForValue().set(activeItemKey, activeJson, Duration.ofMinutes(10));
            return activeItemDTO;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }
  
    public List<ItemDTO> getEndedItemsById(List<Integer> itemIds) {
        if(itemIds.isEmpty()){
            return Collections.emptyList();
        }
        List <Item> items=itemRepo.findAllById(itemIds);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Item> Items = items.stream()
                .filter(item -> item.getAuction() != null
                        && item.getAuction().getStartingTime() != null
                        && item.getAuction().getEndingTime() != null
                        && now.isAfter(item.getAuction().getStartingTime())
                        )    //&& now.isAfter(item.getAuction().getEndingTime()) IMPORTANT : ADD THIS LINE TO GET THE HISTORY OF ENDED ITEMS ONLY
                .toList();
        List<ItemDTO> ItemDTOs = modelMapper.map(Items, new TypeToken<List<ItemDTO>>() {}.getType());
        ItemDTOs.forEach(ItemDTO::updateStatus);
        return ItemDTOs;
    }
}
