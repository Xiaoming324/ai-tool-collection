package com.itheima.ai.controller;

import com.itheima.ai.entity.dto.AuthRequest;
import com.itheima.ai.entity.vo.Result;
import com.itheima.ai.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody AuthRequest req) {
        userService.register(req.getUsername(), req.getPassword());
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody AuthRequest req) {
        String token = userService.login(req.getUsername(), req.getPassword());
        return Result.ok(token);
    }
}
