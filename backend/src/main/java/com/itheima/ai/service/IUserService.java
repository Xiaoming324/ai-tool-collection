package com.itheima.ai.service;

import com.itheima.ai.entity.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IUserService extends IService<User> {
    void register(String username, String password);
    String login(String username, String password); // 返回JWT
}
