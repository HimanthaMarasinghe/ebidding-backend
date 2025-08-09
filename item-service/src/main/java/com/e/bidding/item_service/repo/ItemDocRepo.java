package com.e.bidding.item_service.repo;

import com.e.bidding.item_service.model.ItemDoc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDocRepo extends JpaRepository<ItemDoc, Integer> {
}
