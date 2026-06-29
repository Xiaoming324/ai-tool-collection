package com.itheima.ai.service.impl;

import com.itheima.ai.config.S3Properties;
import com.itheima.ai.entity.dto.StoredObjectInfo;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public StoredObjectInfo uploadImage(Long userId, String chatId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        String bucket = s3Properties.getBucket();
        String originalFilename = StringUtils.hasText(file.getOriginalFilename())
                ? StringUtils.cleanPath(file.getOriginalFilename())
                : "unknown";
        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "application/octet-stream";
        String key = buildImageKey(userId, chatId, originalFilename);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new BusinessException("Failed to read upload file", e);
        } catch (Exception e) {
            throw new BusinessException("Failed to upload file to S3", e);
        }

        return new StoredObjectInfo(
                bucket,
                key,
                originalFilename,
                contentType,
                file.getSize()
        );
    }

    private String buildImageKey(Long userId, String chatId, String originalFilename) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString();
        return "images/" + userId + "/" + chatId + "/" + date + "/" + uuid + "-" + originalFilename;
    }
}
