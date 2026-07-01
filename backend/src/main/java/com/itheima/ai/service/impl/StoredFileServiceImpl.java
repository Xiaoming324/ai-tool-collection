package com.itheima.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.enums.FileKind;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.mapper.StoredFileMapper;
import com.itheima.ai.service.IStoredFileService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoredFileServiceImpl extends ServiceImpl<StoredFileMapper, StoredFile> implements IStoredFileService {

    @Override
    public StoredFile saveFileMetadata(Long userId,
                                       Long sessionId,
                                       FileKind fileKind,
                                       MultipartFile file,
                                       String bucket,
                                       String key) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is empty");
        }
        if (fileKind == null) {
            throw new BusinessException("File kind is required");
        }
        if (!StringUtils.hasText(bucket) || !StringUtils.hasText(key)) {
            throw new BusinessException("Storage location is required");
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            throw new BusinessException("Content type is missing");
        }

        StoredFile storedFile = new StoredFile()
                .setUserId(userId)
                .setSessionId(sessionId)
                .setFileKind(fileKind)
                .setOriginalFilename(StringUtils.hasText(originalFilename) ? originalFilename : key)
                .setContentType(contentType)
                .setSizeBytes(file.getSize())
                .setS3Bucket(bucket)
                .setS3Key(key)
                .setCreatedAt(LocalDateTime.now());
        save(storedFile);
        return storedFile;
    }

    @Override
    public StoredFile getByIdAndUserId(Long fileId, Long userId) {
        return lambdaQuery()
                .eq(StoredFile::getId, fileId)
                .eq(StoredFile::getUserId, userId)
                .one();
    }

    @Override
    public List<StoredFile> listBySessionIdAndFileKind(Long sessionId, FileKind fileKind) {
        return lambdaQuery()
                .eq(StoredFile::getSessionId, sessionId)
                .eq(StoredFile::getFileKind, fileKind)
                .orderByAsc(StoredFile::getCreatedAt)
                .list();
    }

    @Override
    public List<StoredFile> listBySessionId(Long sessionId) {
        return lambdaQuery()
                .eq(StoredFile::getSessionId, sessionId)
                .orderByAsc(StoredFile::getCreatedAt)
                .list();
    }

    @Override
    public StoredFile getLatestBySessionIdAndFileKind(Long sessionId, FileKind fileKind) {
        return lambdaQuery()
                .eq(StoredFile::getSessionId, sessionId)
                .eq(StoredFile::getFileKind, fileKind)
                .orderByDesc(StoredFile::getCreatedAt)
                .last("limit 1")
                .one();
    }
}
