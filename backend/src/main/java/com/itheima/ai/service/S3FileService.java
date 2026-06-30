package com.itheima.ai.service;

import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.entity.dto.StoredObjectInfo;
import org.springframework.web.multipart.MultipartFile;

public interface S3FileService {

    StoredObjectInfo uploadImage(Long userId, String chatId, MultipartFile file);

    StoredObjectInfo uploadPdf(Long userId, String chatId, MultipartFile file);

    String generatePresignedUrl(StoredFile storedFile);

    void deleteObject(StoredFile storedFile);
}
