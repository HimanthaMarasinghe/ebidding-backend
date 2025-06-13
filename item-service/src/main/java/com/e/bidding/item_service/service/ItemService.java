package com.e.bidding.item_service.service;

import com.e.bidding.item_service.dto.ItemDTO;
import com.e.bidding.item_service.model.Item;
import com.e.bidding.item_service.repo.ItemRepo;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public ItemDTO findById(Integer id) {
        return modelMapper.map(itemRepo.findById(id), ItemDTO.class);
    }

    public ItemDTO save(ItemDTO itemDTO) {
        itemRepo.save(modelMapper.map(itemDTO, Item.class));
        return itemDTO;
    }
}
