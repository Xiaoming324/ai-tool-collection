package com.itheima.ai.service.impl;

import com.itheima.ai.entity.po.StoredFile;
import com.itheima.ai.enums.FileKind;
import com.itheima.ai.exception.BusinessException;
import com.itheima.ai.service.PdfIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfIngestionServiceImpl implements PdfIngestionService {
    private final S3Client s3Client;
    private final VectorStore vectorStore;

    @Override
    public void ingestPdf(StoredFile storedFile) {
        if (storedFile == null) {
            throw new BusinessException("Stored file does not exist");
        }
        if (storedFile.getFileKind() != FileKind.PDF) {
            throw new BusinessException("Stored file is not a PDF");
        }
        if (!StringUtils.hasText(storedFile.getS3Bucket()) || !StringUtils.hasText(storedFile.getS3Key())) {
            throw new BusinessException("Stored file location is missing");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(storedFile.getS3Bucket())
                .key(storedFile.getS3Key())
                .build();

        byte[] pdfBytes;
        try {
            pdfBytes = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (Exception e) {
            throw new BusinessException("Failed to read PDF from S3", e);
        }

        ByteArrayResource resource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return storedFile.getOriginalFilename();
            }
        };

        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build()
        );
        List<Document> documents = reader.read();


        List<Document> enrichedDocuments = documents.stream()
                .map(document -> {
                    Map<String, Object> metadata = new HashMap<>(document.getMetadata());
                    metadata.put("userId", storedFile.getUserId());
                    metadata.put("sessionId", storedFile.getSessionId());
                    metadata.put("fileId", storedFile.getId());
                    metadata.put("fileKind", storedFile.getFileKind().getValue());
                    metadata.put("originalFilename", storedFile.getOriginalFilename());
                    metadata.put("s3Bucket", storedFile.getS3Bucket());
                    metadata.put("s3Key", storedFile.getS3Key());
                    return new Document(document.getText(), metadata);
                })
                .toList();

        vectorStore.add(enrichedDocuments);
    }
}
