package com.itheima.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {

    private String region;

    private String bucket;

    private String accessKey;

    private String secretKey;
}
