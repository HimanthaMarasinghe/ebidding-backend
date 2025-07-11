package com.e.bidding.item_service.repo;

import com.e.bidding.item_service.model.Item;
import com.e.bidding.item_service.projection.ItemToScheduleProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepo extends JpaRepository<Item, Integer> {

    @Query("SELECT i FROM Item i WHERE i.id NOT IN (SELECT a.id FROM Auction a)")
    List<Item> findItemsWithNoAuction();

    @Query("""
        SELECT i FROM Item i
        JOIN Auction a ON i.id = a.id
        WHERE a.startingTime > CURRENT_TIMESTAMP
    """)
    List<Item> findPendingItems();

    @Query("""
        SELECT i FROM Item i
        JOIN Auction a ON i.id = a.id
        WHERE a.startingTime <= CURRENT_TIMESTAMP
          AND a.endingTime > CURRENT_TIMESTAMP
    """)
    List<Item> findActiveItems();

    @Query("""
        SELECT i FROM Item i
        JOIN Auction a ON i.id = a.id
        WHERE a.endingTime <= CURRENT_TIMESTAMP
    """)
    List<Item> findCompleteItems();

    @Query("""
        SELECT i.id AS id, i.caseNumber AS caseNumber, i.title AS title,
               i.startingBid AS startingBid, i.increment AS increment
        FROM Item i 
        WHERE i.id NOT IN (SELECT a.id FROM Auction a)
        AND i.id IN :ids
    """)
    List<ItemToScheduleProjection> itemsToSchedule(@Param("ids") List<Integer> ids);

}
