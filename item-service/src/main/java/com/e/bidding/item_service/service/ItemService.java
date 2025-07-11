package com.e.bidding.item_service.service;

import com.e.bidding.item_service.dto.ItemDTO;
import com.e.bidding.item_service.dto.ResponseDTO;
import com.e.bidding.item_service.model.Item;
import com.e.bidding.item_service.projection.ItemToScheduleProjection;
import com.e.bidding.item_service.repo.ItemRepo;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemService {

    private final ItemRepo itemRepo;
    private final ModelMapper modelMapper;

    public ItemService(ItemRepo itemRepo, ModelMapper modelMapper) {
        this.itemRepo = itemRepo;
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
        List<Item> items = itemRepo.findPendingItems();
        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
    }

    public List<ItemDTO> findActive() {
        List<Item> items = itemRepo.findActiveItems();
        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
    }

    public List<ItemDTO> findComplete() {
        List<Item> items = itemRepo.findCompleteItems();
        return modelMapper.map(items, new TypeToken<List<ItemDTO>>() {}.getType());
    }

    public ItemDTO findById(Integer id) {
        return modelMapper.map(itemRepo.findById(id), ItemDTO.class);
    }

    public List<ItemToScheduleProjection> itemsToSchedule(List<Integer> ids) {
        return itemRepo.itemsToSchedule(ids);
    }

    /**
     * Save one Item, with or without auction details
     * @param itemDTO ItemDTO that should be saved
     * @return A response object that include the success status, saved data and a message
     */
    public ResponseDTO<ItemDTO> save(ItemDTO itemDTO) {
        Item newItem = modelMapper.map(itemDTO, Item.class);
        if (newItem.getAuction() != null)
            newItem.getAuction().setItem(newItem);
        Item savedItem = itemRepo.save(newItem);
        ItemDTO savedItemDTO = modelMapper.map(savedItem, ItemDTO.class);
        return new ResponseDTO<>(true, savedItemDTO, "Item saved successfully");
    }

    /**
     * Save a list ot items at once.
     * @param itemDTOs List of items that should be saved
     * @return List of items that got saved
     */
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

}
