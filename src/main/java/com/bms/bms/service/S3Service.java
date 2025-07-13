package com.bms.bms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.io.File;
import java.net.URI;

@Service
public class S3Service {

    @Value("${cloudflare.r2.accessKey}")
    private String accessKey;

    @Value("${cloudflare.r2.secretKey}")
    private String secretKey;

    @Value("${cloudflare.r2.endpoint}")
    private String endPoint;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.publicurl}")
    private String publicURL;

    private S3Client s3Client;

    private void initializeClient() {
        if (s3Client != null) return;

        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3Configuration serviceConfig = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)
                .build();

        s3Client = S3Client.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .region(Region.of("eu-north-1"))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(serviceConfig)
                .build();
    }

    public String Upload(MultipartFile imageMP, Long id) {
        initializeClient();
        try {
            File file = File.createTempFile("cover-" + id, null);
            imageMP.transferTo(file);

            s3Client.putObject(request -> request
                            .bucket(bucket)
                            .key("cover-" + id)
                            .contentType(imageMP.getContentType()),
                    file.toPath());
            file.delete();
            return (publicURL + "cover-" + id);
        } catch (Exception e) {
            return null;
        }
    }
}
