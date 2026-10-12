package com.exchange.user.service;

import com.exchange.user.dto.CreateUserRequest;
import com.exchange.user.dto.UserResponse;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);
}
