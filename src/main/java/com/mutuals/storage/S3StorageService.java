package com.mutuals.storage;

import com.mutuals.common.exception.StorageException;
import com.mutuals.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "mutuals.storage.provider", havingValue = "s3")
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;

    public S3StorageService(AppProperties properties) {
        this.bucket = properties.storage().s3Bucket();
        this.region = properties.storage().s3Region();
        this.s3Client = S3Client.builder().region(Region.of(region)).build();
    }

    @Override
    public String store(MultipartFile file, String folder) {
        String extension = ImageValidator.extensionFor(file);
        String key = folder + "/" + UUID.randomUUID() + "." + extension;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | SdkException ex) {
            throw new StorageException("Could not upload file to S3");
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
