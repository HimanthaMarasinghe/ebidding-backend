package com.e.bidding.springSecurity.service;

import com.e.bidding.springSecurity.model.Users;
import com.e.bidding.springSecurity.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // In-memory store for refresh tokens (replace with a database in production)
    private Map<String, String> refreshTokenStore = new HashMap<>();

    public Users register(Users user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public String verify(Users user) {
        //System.out.println(user);
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername());
        }
        return "fail";
    }

    public boolean isValidRefreshToken(String refreshToken) {
        System.out.println("Checking refreshTokenStore: " + refreshTokenStore);
        return refreshTokenStore.containsKey(refreshToken); // Check if token exists
    }

    public String generateNewToken(String refreshToken) {
        if (isValidRefreshToken(refreshToken)) {
            String username = refreshTokenStore.get(refreshToken); // Placeholder logic
            if (username != null) {
                return jwtService.generateToken(username);
            }
        }
        return null;
    }

    public void storeRefreshToken(String refreshToken, String username) {
        refreshTokenStore.put(refreshToken, username); // Store with associated username
    }
}