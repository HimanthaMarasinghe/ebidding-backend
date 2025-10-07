package com.e.bidding.bidding_service.controller;

import com.e.bidding.bidding_service.dto.MyBidsDTO;
import com.e.bidding.bidding_service.service.UserService;
import com.e.bidding.dtos.ItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

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

    @GetMapping("/getMyBidItems/{username}")
    public ResponseEntity<ArrayList<MyBidsDTO>> getMyBidItems(@PathVariable String username){
        if(username.isEmpty()||username==""){
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok().body(userService.getItemsForUser(username));
    }
}
