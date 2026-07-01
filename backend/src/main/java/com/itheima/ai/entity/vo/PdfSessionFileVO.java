package com.itheima.ai.entity.vo;

import com.itheima.ai.entity.po.StoredFile;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PdfSessionFileVO {

    private Long fileId;

    private String chatId;

    private String originalFilename;

    private String url;

    public PdfSessionFileVO(String chatId, StoredFile storedFile, String url) {
        this.fileId = storedFile.getId();
        this.chatId = chatId;
        this.originalFilename = storedFile.getOriginalFilename();
        this.url = url;
    }
}
