package com.itheima.ai.service.impl;

import com.itheima.ai.entity.po.User;
import com.itheima.ai.mapper.UserMapper;
import com.itheima.ai.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(String username, String password) {
        // 1.查重名
        User existing = lambdaQuery().eq(User::getUsername, username).one();
        if (existing != null) {
            throw new BusinessException("Username already exists");
        }

        // 2.密码加密
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        save(user);
    }

    @Override
    public String login(String username, String password) {
        User user = lambdaQuery().eq(User::getUsername, username).one();
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }
}
