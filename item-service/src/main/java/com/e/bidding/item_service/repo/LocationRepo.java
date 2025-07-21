package com.e.bidding.item_service.repo;

import com.e.bidding.item_service.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepo  extends JpaRepository<Location, Integer> {
}
