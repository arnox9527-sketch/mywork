package com.exchange.user.dto;

import lombok.Data;

@Data
public class CreateUserRequest {

    private String email;

    private String phone;

    private String password;

    private String nickname;
}
