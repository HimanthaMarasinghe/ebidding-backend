package com.e.bidding.user_service.repo;

import com.e.bidding.user_service.model.YardManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YardManagerRepo extends JpaRepository<YardManager, Integer> {
    @Modifying
    @Query("UPDATE YardManager y SET y.yard_id = :yardId WHERE y.id = :manId")
    void updateYardId(@Param("yardId") Integer yardId, @Param("manId") Integer manId);

    Optional<YardManager> findByUsername(String username);
}