package br.com.fiap.fiapx.video.infra.storage;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public String uploadVideo(MultipartFile file, String userEmail) {
        String key = "videos/" + userEmail + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("Video uploaded to MinIO: {}", key);
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao fazer upload do vídeo: " + e.getMessage(), e);
        }
    }

    public InputStream downloadFile(String key) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new RuntimeException("Falha ao baixar arquivo: " + e.getMessage(), e);
        }
    }

    public String getPresignedDownloadUrl(String key) {
        try {
            return minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket).object(key)
                            .method(io.minio.http.Method.GET)
                            .expiry(3600)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar URL de download: " + e.getMessage(), e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
