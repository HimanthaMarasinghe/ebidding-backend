package com.e.bidding.user_service.controller;

import com.e.bidding.user_service.dto.TokenAdd;
import com.e.bidding.user_service.dto.TokenRemove;
import com.e.bidding.user_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/us/v1")
@CrossOrigin(origins = "*")

public class NotificationController {

    @Autowired
    NotificationService notificationService;
    @PostMapping("/addToken")

    public ResponseEntity<String> addToken(@RequestBody TokenAdd tokenRequest){
        try{
            String username= tokenRequest.getUsername();
            String Token= tokenRequest.getPushToken();
            String response=notificationService.saveUserPushToken(username,Token);
            return ResponseEntity.ok(response);
        }
        catch(Exception e) {
            return ResponseEntity.status(500).body("Error saving token: " + e.getMessage());
        }
    }

    @PostMapping("/removeToken")
    public ResponseEntity <String> removeToken(@RequestBody TokenRemove token){
        try{
            boolean status=notificationService.removeToken(token.getPushToken());
            if(status){
                return ResponseEntity.ok("Token Deleted");

            }
            else{
                return ResponseEntity.ok("Token Not Found");

            }

        }
        catch (Exception e){
            System.out.println(e);
            return ResponseEntity.status(500).body("Error Deleting Token");
        }
    }

}
