package com.exchange.user.controller;

import com.exchange.user.dto.CreateUserRequest;
import com.exchange.user.dto.Result;
import com.exchange.user.dto.UserResponse;
import com.exchange.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/createUser")
    public Result<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserResponse response = userService.createUser(request);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }
}
