package com.itheima.ai.entity.vo;

import com.itheima.ai.entity.po.StoredFile;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AttachmentVO {
    private Long fileId;
    private String fileKind;
    private String originalFilename;
    private String contentType;
    private Long sizeBytes;
    private String url;

    public AttachmentVO(StoredFile storedFile) {
        this(storedFile, null);
    }

    public AttachmentVO(StoredFile storedFile, String url) {
        this.fileId = storedFile.getId();
        this.fileKind = storedFile.getFileKind() == null ? "" : storedFile.getFileKind().getValue();
        this.originalFilename = storedFile.getOriginalFilename();
        this.contentType = storedFile.getContentType();
        this.sizeBytes = storedFile.getSizeBytes();
        this.url = url;
    }
}
