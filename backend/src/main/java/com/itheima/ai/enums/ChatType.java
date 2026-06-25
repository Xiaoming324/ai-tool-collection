package com.itheima.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatType {
    CHAT("chat"),
    PDF("pdf"),
    TRAVEL("travel");

    @EnumValue   // MyBatis-Plus：存进/读出数据库时用这个值（存 "chat" 而不是 "CHAT"）
    @JsonValue   // Jackson：JSON 序列化/反序列化用这个值
    private final String value;

    ChatType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
