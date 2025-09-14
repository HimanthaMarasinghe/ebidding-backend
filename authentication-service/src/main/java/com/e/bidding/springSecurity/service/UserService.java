package com.e.bidding.springSecurity.service;

import com.e.bidding.dtos.AuthUserCreationDTO;
import com.e.bidding.dtos.UserRegistrationDTO;
import com.e.bidding.dtos.ValidateRoleRequest;
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
import org.apache.commons.lang3.tuple.Pair;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<String, Pair<String, String>> refreshTokenStore = new ConcurrentHashMap<>();

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

    public Boolean validateSystemUsers(ValidateRoleRequest request){

        String username = request.getUsername();
        String role = request.getRole();

        String storedRole = userRepo.findRoleByUsername(username);
        if (storedRole == null) {
            return false;
        }

        return storedRole.equalsIgnoreCase(role);

    }

    public String verify(Users user) {
        //System.out.println(user);
        Users loggedUser = userRepo.findByUsername(user.getUsername());

        if (loggedUser == null) {
            return "fail";
        }

        if(encoder.matches(user.getPassword(), loggedUser.getPassword())) {
            System.out.println("Password verified, generating token for role: " + loggedUser.getRole());
            return jwtService.generateToken(user.getUsername(), loggedUser.getRole());
        }

        return "fail";
    }

    public boolean isValidRefreshToken(String refreshToken) {
        System.out.println("Checking refreshTokenStore: " + refreshTokenStore);
        return refreshTokenStore.containsKey(refreshToken); // Check if token exists
    }

    public String generateNewToken(String refreshToken) {
        if (isValidRefreshToken(refreshToken)) {
            Pair<String, String> data = refreshTokenStore.get(refreshToken);
            if (data != null) {
                String username = data.getLeft();
                String role = data.getRight();
                return jwtService.generateToken(username, role);
            }
        }
        return null;
    }

    public Users getUserData(String refreshToken) {
        if (isValidRefreshToken(refreshToken)) {
            Pair<String, String> userInfo = refreshTokenStore.get(refreshToken);
            if (userInfo != null) {
                String username = userInfo.getLeft(); // username
                return userRepo.findByUsername(username);
            }
        }
        return null;
    }

    public void storeRefreshToken(String refreshToken, String username, String role) {
        refreshTokenStore.put(refreshToken, Pair.of(username, role));
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