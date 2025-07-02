package com.e.bidding.bidding_service.controller;

import com.e.bidding.bidding_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("hello")
    public String hello(){
        return userService.hello();
    }
}
