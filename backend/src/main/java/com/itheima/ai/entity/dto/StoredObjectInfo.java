package com.itheima.ai.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectInfo {

    private String bucket;

    private String key;

    private String originalFilename;

    private String contentType;

    private Long size;
}
