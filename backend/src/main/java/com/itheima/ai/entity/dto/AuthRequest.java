package com.itheima.ai.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank(message = "username is required")
    @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "username: letters/digits/symbols only, no spaces")
    @Size(min = 3, max = 20, message = "username length 3-20")
    private String username;

    @NotBlank(message = "password is required")
    @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "password: letters/digits/symbols only, no spaces")
    @Size(min = 6, max = 20, message = "password length 6-20")
    private String password;
}
