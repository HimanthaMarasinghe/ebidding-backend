package com.e.bidding.item_service.repo;

import com.e.bidding.item_service.model.Item;
import com.e.bidding.item_service.projection.ItemToScheduleProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemRepo extends JpaRepository<Item, Integer> {

    @Query("SELECT i FROM Item i WHERE i.id NOT IN (SELECT a.id FROM Auction a)")
    List<Item> findItemsWithNoAuction();

    @Query("""
        SELECT i FROM Item i
        JOIN i.auction a
        WHERE a.startingTime > :now
    """)
    List<Item> findPendingItems(@Param("now") LocalDateTime now);

    @Query("""
        SELECT i FROM Item i
        JOIN Auction a ON i.id = a.id
        WHERE a.startingTime <=:now
          AND a.endingTime >:now
    """)
    List<Item> findActiveItems(@Param("now") LocalDateTime now);

    @Query("""
        SELECT i FROM Item i
        JOIN Auction a ON i.id = a.id
        WHERE a.endingTime <=:now
    """)
    List<Item> findCompleteItems(@Param("now") LocalDateTime now);

    @Query("""
        SELECT i.id AS id, i.caseNumber AS caseNumber, i.title AS title,
               i.startingBid AS startingBid, i.increment AS increment
        FROM Item i 
        WHERE i.id NOT IN (SELECT a.id FROM Auction a)
        AND i.id IN :ids
    """)
    List<ItemToScheduleProjection> itemsToSchedule(@Param("ids") List<Integer> ids);

    @Query(value = """
    SELECT *, ts_rank(
        setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(description, '')), 'B'),
        plainto_tsquery('english', :term)
    ) AS rank
    FROM item
    WHERE
        setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(description, '')), 'B')
        @@ plainto_tsquery('english', :term)
    ORDER BY rank DESC
""", nativeQuery = true)
    List<Item> searchByTerm(@Param("term") String term);

}
