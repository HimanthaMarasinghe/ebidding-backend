package com.e.bidding.item_service.repo;

import com.e.bidding.item_service.model.ItemSpecs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemSpecsRepo  extends JpaRepository<ItemSpecs, Integer> {
}
