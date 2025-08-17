package com.e.bidding.item_service.repo;

import com.e.bidding.item_service.model.Favorite;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepo extends JpaRepository<Favorite, Integer> {
    List<Favorite> findAllByUserId(Integer userId);

}
