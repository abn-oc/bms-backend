package com.bms.bms.dto;

import com.bms.bms.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private User user;
}
