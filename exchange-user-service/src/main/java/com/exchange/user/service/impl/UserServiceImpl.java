package com.exchange.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exchange.user.converter.UserConverter;
import com.exchange.user.dto.CreateUserRequest;
import com.exchange.user.dto.UserResponse;
import com.exchange.user.entity.User;
import com.exchange.user.mapper.UserMapper;
import com.exchange.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (emailCount > 0) {
            throw new IllegalArgumentException("邮箱已注册: " + request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, request.getPhone()));
            if (phoneCount > 0) {
                throw new IllegalArgumentException("手机号已注册: " + request.getPhone());
            }
        }

        User user = UserConverter.toEntity(request, passwordEncoder.encode(request.getPassword()));
        userMapper.insert(user);

        return UserConverter.toResponse(user);
    }
}
