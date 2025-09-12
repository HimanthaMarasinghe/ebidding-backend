package com.e.bidding.user_service.repo;

import com.e.bidding.user_service.model.Bidder;
import com.e.bidding.user_service.model.PushTokens;
import com.e.bidding.user_service.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokensRepo extends JpaRepository<PushTokens,Integer> {
    Optional<PushTokens> findByUserAndToken(UserProfile user, String token);

    List<PushTokens> findByUser(UserProfile user);

    void deleteByToken(String token);


}
