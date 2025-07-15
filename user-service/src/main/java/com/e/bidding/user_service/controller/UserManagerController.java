package com.e.bidding.user_service.controller;

import com.e.bidding.dtos.AuthUserCreationDTO;
import com.e.bidding.dtos.ProfileCreationEventDTO;
import com.e.bidding.dtos.UserAddingDTO;
import com.e.bidding.user_service.kafka.AuthUserProducer;
import com.e.bidding.user_service.model.UserProfile;
import com.e.bidding.user_service.service.UserProfileCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserManagerController {

    @Autowired
    private AuthUserProducer authUserProducer;

    @Autowired
    private UserProfileCreationService userProfileCreationService;

    @PostMapping("/addUser")
    public ResponseEntity<Map<String, String>> addUser(@RequestBody UserAddingDTO userAddingDTO) {
        try {
            UserProfile userProfile = userProfileCreationService.addUserProfile(userAddingDTO);
            AuthUserCreationDTO event = new AuthUserCreationDTO();
            event.setUsername(userAddingDTO.getUsername());
            event.setEmail(userAddingDTO.getEmail());
            event.setRole(userAddingDTO.getRole());
            event.setPrimary_phone(userAddingDTO.getPrimary_phone());
            authUserProducer.sendMessage(event);
            return ResponseEntity.ok(Map.of("message", "User added successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "User adding failed: " + e.getMessage()));
        }
    }

    @PostMapping("/hello")
    public String hello(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            System.out.println("Hello endpoint hit with token: " + jwtToken);
            return "hello";
        }
        // No valid JWT, return 401 to trigger refresh
        throw new SecurityException("Missing or invalid JWT token");
    }
}
