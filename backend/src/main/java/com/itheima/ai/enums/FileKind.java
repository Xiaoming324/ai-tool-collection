package com.itheima.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FileKind {
    IMAGE("image"),
    PDF("pdf");

    @EnumValue
    @JsonValue
    private final String value;

    FileKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
