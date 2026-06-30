package com.itheima.ai.service;

import com.itheima.ai.entity.po.StoredFile;

public interface PdfIngestionService {
    void ingestPdf(StoredFile storedFile);
}

