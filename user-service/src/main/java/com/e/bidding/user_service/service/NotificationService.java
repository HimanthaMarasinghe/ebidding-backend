package com.e.bidding.user_service.service;

import com.e.bidding.user_service.model.PushTokens;
import com.e.bidding.user_service.model.UserProfile;
import com.e.bidding.user_service.repo.PushTokensRepo;
import com.e.bidding.user_service.repo.UserProfileRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private PushTokensRepo pushTokensRepo;

    @Autowired
    private UserProfileRepo userProfileRepo;

    @Transactional
    public String saveUserPushToken(String username,String pushToken) {
        if(!userProfileRepo.existsByUsername(username)){
            throw new RuntimeException("User not found with username: " + username);
        }
        UserProfile user = userProfileRepo.findByUsername(username);
        boolean exists = pushTokensRepo.findByUserAndToken(user, pushToken).isPresent();

        if (exists) {
            System.out.println("Token already exists for user " + user.getId());

            return "A Token Already Exists ";
        }

        PushTokens token = new PushTokens();
        token.setToken(pushToken);
        token.setUser(user);
        pushTokensRepo.save(token);
//        System.out.println(getPushTokensByUserID(userId));

        return "Token saved successfully";


    }


    public List<String> getPushTokensByUserID(Integer userId){
        UserProfile user = userProfileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return pushTokensRepo.findByUser(user).stream().map(PushTokens::getToken).toList();
    }
    @Transactional
    public boolean removeToken(String token){
        pushTokensRepo.deleteByToken(token);
        return true;
    }

}
