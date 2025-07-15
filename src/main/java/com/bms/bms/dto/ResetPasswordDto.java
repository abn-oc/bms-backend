package com.bms.bms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDto {
    private String code;
    private String newPassword;
}
