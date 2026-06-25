package com.itheima.ai.config;

import com.itheima.ai.enums.ChatType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToChatTypeConverter implements Converter<String, ChatType> {
    @Override
    public ChatType convert(String source) {
        for (ChatType t : ChatType.values()) {
            if (t.getValue().equalsIgnoreCase(source)) return t;
        }
        throw new IllegalArgumentException("Unknown chat type: " + source);
    }
}
