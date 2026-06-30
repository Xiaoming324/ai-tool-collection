package com.itheima.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.enums.FileKind;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IStoredFileService extends IService<StoredFile> {
    StoredFile saveFileMetadata(Long userId,
                                Long sessionId,
                                FileKind fileKind,
                                MultipartFile file,
                                String bucket,
                                String key);

    StoredFile getByIdAndUserId(Long fileId, Long userId);

    List<StoredFile> listBySessionIdAndFileKind(Long sessionId, FileKind fileKind);

    List<StoredFile> listBySessionId(Long sessionId);

}
