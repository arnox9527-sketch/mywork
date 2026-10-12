package com.exchange.user.converter;

import com.exchange.user.dto.CreateUserRequest;
import com.exchange.user.dto.UserResponse;
import com.exchange.user.entity.User;

public class UserConverter {

    private UserConverter() {
    }

    public static User toEntity(CreateUserRequest request, String encodedPassword) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(encodedPassword);
        user.setNickname(request.getNickname());
        user.setStatus(1);
        user.setDeleted(0);
        return user;
    }

    public static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setStatus(user.getStatus());
        response.setCreatedTime(user.getCreatedTime());
        return response;
    }
}
