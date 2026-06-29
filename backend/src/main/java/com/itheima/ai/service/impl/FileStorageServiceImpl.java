package com.itheima.ai.service.impl;

import com.itheima.ai.config.S3Properties;
import com.itheima.ai.entity.dto.StoredObjectInfo;
import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(30);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
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

    @Override
    public String generatePresignedUrl(StoredFile storedFile) {
        if (storedFile == null) {
            throw new BusinessException("Stored file does not exist");
        }
        if (!StringUtils.hasText(storedFile.getS3Bucket()) || !StringUtils.hasText(storedFile.getS3Key())) {
            throw new BusinessException("Stored file location is missing");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(storedFile.getS3Bucket())
                .key(storedFile.getS3Key())
                .responseContentType(storedFile.getContentType())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_DURATION)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    private String buildImageKey(Long userId, String chatId, String originalFilename) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString();
        return "images/" + userId + "/" + chatId + "/" + date + "/" + uuid + "-" + originalFilename;
    }
}
