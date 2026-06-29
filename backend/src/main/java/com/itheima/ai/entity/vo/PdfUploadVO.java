package com.itheima.ai.entity.vo;

import com.itheima.ai.entity.po.StoredFile;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PdfUploadVO {

    private Long fileId;

    private String chatId;

    private String originalFilename;

    public PdfUploadVO(String chatId, StoredFile storedFile) {
        this.chatId = chatId;
        this.fileId = storedFile.getId();
        this.originalFilename = storedFile.getOriginalFilename();
    }
}
