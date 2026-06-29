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

    public AttachmentVO(StoredFile storedFile) {
        this.fileId = storedFile.getId();
        this.fileKind = storedFile.getFileKind() == null ? "" : storedFile.getFileKind().getValue();
        this.originalFilename = storedFile.getOriginalFilename();
    }
}
