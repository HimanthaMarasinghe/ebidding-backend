package com.e.bidding.springSecurity.controller;

import com.e.bidding.dtos.ProfileCreationEventDTO;
import com.e.bidding.dtos.UserRegistrationDTO;
import com.e.bidding.dtos.ValidateRoleRequest;
import com.e.bidding.springSecurity.kafka.UserProducer;
import com.e.bidding.springSecurity.model.Users;
import com.e.bidding.springSecurity.service.JWTService;
import com.e.bidding.springSecurity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserProducer userProducer;

    @Autowired
    private KafkaTemplate<String, ProfileCreationEventDTO> kafkaTemplate;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestPart("username") String username,
            @RequestPart("password") String password,
            @RequestPart("email") String email,
            @RequestPart("first_name") String firstName,
            @RequestPart("last_name") String lastName,
            @RequestPart("primary_phone") String primaryPhone,
            @RequestPart("secondary_phone") String secondaryPhone,
            @RequestPart("date_of_birth") String dateOfBirth,
            @RequestPart(value = "user_image", required = false) MultipartFile userImage,
            @RequestPart(value = "nic_image", required = false) MultipartFile nicImage) {
        try {
            System.out.println(username);
            // Validate required fields
            if (username.isEmpty() || password.isEmpty() || email.isEmpty() ||
                    firstName.isEmpty() || lastName.isEmpty() || primaryPhone.isEmpty() ||
                    secondaryPhone.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "All fields are required"));
            }

            // Validate email format
            if (!email.matches("\\S+@\\S+\\.\\S+")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid email address"));
            }

            // Validate password length
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
            }

            // Handle file uploads
            String userImageUrl = "default_user_image.jpg";
            String nicImageUrl = "default_nic_image.jpg";

            // Define the upload directory
            String uploadDir = "D:\\3rd year project\\uploads\\user_images"; // Ensure this directory exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save user image
            if (userImage != null && !userImage.isEmpty()) {
                String userImageFileName = username + "_userImage.jpg";
                Path userImagePath = uploadPath.resolve(userImageFileName);
                Files.write(userImagePath, userImage.getBytes());
                userImageUrl = userImageFileName;
            }

            // Save NIC image
            if (nicImage != null && !nicImage.isEmpty()) {
                String nicImageFileName = username + "_nicImage.jpg";
                Path nicImagePath = uploadPath.resolve(nicImageFileName);
                Files.write(nicImagePath, nicImage.getBytes());
                nicImageUrl = nicImageFileName;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate dob = LocalDate.parse(dateOfBirth, formatter);

            // Create UserRegistrationDTO
            UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
            userRegistrationDTO.setUsername(username);
            userRegistrationDTO.setPassword(password);
            userRegistrationDTO.setEmail(email);
            userRegistrationDTO.setFirst_name(firstName);
            userRegistrationDTO.setLast_name(lastName);
            userRegistrationDTO.setPrimary_phone(primaryPhone);
            userRegistrationDTO.setSecondary_phone(secondaryPhone);
            userRegistrationDTO.setDate_of_birth(dob);
            userRegistrationDTO.setUser_image_url(userImageUrl);
            userRegistrationDTO.setNic_image_url(nicImageUrl);

            // Register user
            Users user = userService.register(userRegistrationDTO);

            // Create profile creation event
            ProfileCreationEventDTO event = new ProfileCreationEventDTO();
            event.setUsername(username);
            event.setEmail(email);
            event.setDate_of_birth(dob);
            event.setRole("Bidder");
            event.setPrimary_phone(primaryPhone);
            event.setSecondary_phone(secondaryPhone);
            event.setFirst_name(firstName);
            event.setLast_name(lastName);
            event.setUser_image_url(userImageUrl);
            event.setNic_image_url(nicImageUrl);

            // Send event to message queue
            userProducer.sendMessage(event);

            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (Exception e) {
            System.out.println("errp");
            return ResponseEntity.status(500).body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Users user) {
        String token = userService.verify(user);
        Users loggedUser = userService.getUserByUsername(user.getUsername());
        System.out.println("this is from login" + token);
        if (!token.equals("fail")) {
            String refreshToken = UUID.randomUUID().toString();
            userService.storeRefreshToken(refreshToken, user.getUsername(), user.getRole()); // Store the token
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
            response.put("role", loggedUser.getRole());
            response.put("username", loggedUser.getUsername());

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
                Users loggedUser = userService.getUserData(refreshToken); //to get role and username
                System.out.println(loggedUser);
                String newJwtToken = jwtService.generateToken(loggedUser.getUsername(), loggedUser.getRole());
                System.out.println("new token created: " + newJwtToken);
                if (newJwtToken != null) {
                    Map<String, String> response = new HashMap<>();
                    response.put("jwtToken", newJwtToken);
                    response.put("message", "Token refreshed");
                    response.put("role", loggedUser.getRole());
                    response.put("username", loggedUser.getUsername());
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

    @PostMapping("/validate-role")
    public ResponseEntity<?> validateRole(@RequestBody ValidateRoleRequest request) {
        System.out.println("Validating user: " + request.getUsername() + " with role: " + request.getRole());

        boolean isValid = userService.validateSystemUsers(request);

        if (isValid) {
            return ResponseEntity.ok("User role validated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid role for user");
        }
    }

    @PostMapping("/hello")
    public String hello() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return username;
    }
}