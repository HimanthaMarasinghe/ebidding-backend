package com.e.bidding.springSecurity.service;

import com.e.bidding.dtos.AuthUserCreationDTO;
import com.e.bidding.dtos.UserRegistrationDTO;
import com.e.bidding.springSecurity.model.Users;
import com.e.bidding.springSecurity.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // In-memory store for refresh tokens (replace with a database in production)
    private Map<String, String> refreshTokenStore = new HashMap<>();

    public Users register(UserRegistrationDTO userRegistrationDTO) {
        Users user = new Users();
        user.setUsername(userRegistrationDTO.getUsername());
        user.setRole("Bidder");
        user.setEmail(userRegistrationDTO.getEmail());
        user.setPrimaryPhone(userRegistrationDTO.getPrimary_phone());
        user.setPassword(encoder.encode(userRegistrationDTO.getPassword()));
        System.out.println(user);
        return userRepo.save(user);
    }

    public void addUser(AuthUserCreationDTO authUserCreationDTO) {
        Users user = new Users();
        user.setUsername(authUserCreationDTO.getUsername());
        user.setRole(authUserCreationDTO.getRole());
        user.setEmail(authUserCreationDTO.getEmail());
        user.setPrimaryPhone(authUserCreationDTO.getPrimary_phone());
        String rawPassword = "hello";  //generateRandomPassword(12) use hello for testing
        user.setPassword(encoder.encode(rawPassword));
        System.out.println(user);
        userRepo.save(user);
    }

    public String verify(Users user) {
        //System.out.println(user);
        Users loggedUser = userRepo.findByUsername(user.getUsername());
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

    public Users getUserData(String refreshToken){
        if (isValidRefreshToken(refreshToken)) {
            String username = refreshTokenStore.get(refreshToken); // Placeholder logic
            return userRepo.findByUsername(username);
        }

        return null;
    }

    public void storeRefreshToken(String refreshToken, String username) {
        refreshTokenStore.put(refreshToken, username); // Store with associated username
    }

    public Users getUserByUsername(String username){
        return userRepo.findByUsername(username);
    }

    public void rollbackRegistration(String username) {
        Users user = userRepo.findByUsername(username);
        if (user != null) {
            userRepo.delete(user);
            System.out.println("Rolled back registration for user: " + username);
        }
    }

    private String generateRandomPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        String all = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure at least one of each category
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Fill remaining with random characters
        for (int i = 4; i < length; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // Shuffle to avoid predictable order
        List<Character> chars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);
        StringBuilder shuffled = new StringBuilder();
        chars.forEach(shuffled::append);

        return shuffled.toString();
    }

}