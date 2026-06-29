package com.itheima.ai.service;

import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.entity.dto.StoredObjectInfo;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredObjectInfo uploadImage(Long userId, String chatId, MultipartFile file);

    String generatePresignedUrl(StoredFile storedFile);
}
