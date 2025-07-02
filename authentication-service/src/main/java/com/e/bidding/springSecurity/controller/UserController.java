package com.e.bidding.springSecurity.controller;

import com.e.bidding.springSecurity.model.Users;
import com.e.bidding.springSecurity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Users register(@RequestBody Users user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Users user) {
        String token = userService.verify(user);
        System.out.println("this is from login" + token);
        if (!token.equals("fail")) {
            String refreshToken = UUID.randomUUID().toString();
            userService.storeRefreshToken(refreshToken, user.getUsername()); // Store the token
            System.out.println("Storing refresh token: " + refreshToken + " for user: " + user.getUsername());
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            Map<String, String> response = new HashMap<>();
            response.put("jwtToken", token);
            response.put("message", "Login successful");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(response);
        }
        return ResponseEntity.status(401).body(Map.of("message", "Login failed"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        System.out.println("refresh token called with value: " + refreshToken);
        if (refreshToken != null) {
            System.out.println("Checking validity of refresh token: " + refreshToken);
            if (userService.isValidRefreshToken(refreshToken)) {
                System.out.println("Refresh token is valid");
                String newJwtToken = userService.generateNewToken(refreshToken);
                System.out.println("new token created: " + newJwtToken);
                if (newJwtToken != null) {
                    Map<String, String> response = new HashMap<>();
                    response.put("jwtToken", newJwtToken);
                    response.put("message", "Token refreshed");
                    return ResponseEntity.ok().body(response);
                }
            } else {
                System.out.println("Refresh token invalid or not found in store");
            }
        } else {
            System.out.println("No refresh token cookie found");
        }
        return ResponseEntity.status(401).body(Map.of("message", "Invalid refresh token"));
    }

    @PostMapping("/hello")
    public String hello(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            // Validate token (implement in jwtCookieFilter or here)
            System.out.println("Hello endpoint hit with token: " + jwtToken);
            return "hello";
        }
        // No valid JWT, return 401 to trigger refresh
        throw new SecurityException("Missing or invalid JWT token");
    }
}