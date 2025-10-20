package com.e.bidding.bidding_service.repo;

import com.e.bidding.bidding_service.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepo extends JpaRepository<Bid, Integer> {

    @Query("""
        SELECT b FROM Bid b
        WHERE b.itemId = :itemId
        ORDER BY b.bidTime DESC
    """)
    List<Bid> getAllBidsForItem(@Param("itemId") int itemId);

    //get the current highest bid
    Optional<Bid> findTopByItemIdOrderByAmountDesc(Integer itemId);

    List<Bid> findByBidderUserName(String bidderUserName);

    Optional<Bid> findTopByItemIdAndBidderUserNameOrderByAmountDesc(Integer itemId, String bidderUserName);

    long countByItemId(Integer itemId);

    @Query(value = """
        SELECT * FROM bid 
        WHERE item_id = :itemId
        ORDER BY amount DESC
        OFFSET :offset ROWS FETCH NEXT 1 ROWS ONLY
    """, nativeQuery = true)
    Optional<Bid> findBidByItemIdAndRank(@Param("itemId") int itemId, @Param("offset") int offset);

     default Optional<Bid> findBidByPlace(int itemId, int place) {
        if (place <= 0) {
            throw new IllegalArgumentException("Place must be greater than 0");
        }
        int offset = place - 1;
        return findBidByItemIdAndRank(itemId, offset);
    }
}
