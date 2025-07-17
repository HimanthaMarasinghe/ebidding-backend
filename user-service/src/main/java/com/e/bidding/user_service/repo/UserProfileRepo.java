package com.e.bidding.user_service.repo;

import com.e.bidding.user_service.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepo extends JpaRepository<UserProfile, Integer > {

    UserProfile findByUsername(String username);
}