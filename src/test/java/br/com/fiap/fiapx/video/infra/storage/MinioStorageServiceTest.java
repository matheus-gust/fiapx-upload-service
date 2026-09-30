package br.com.fiap.fiapx.video.infra.storage;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    private static final String BUCKET = "fiapx-videos";

    @Mock MinioClient minioClient;

    MinioStorageService service;

    @BeforeEach
    void setUp() {
        service = new MinioStorageService(minioClient);
        ReflectionTestUtils.setField(service, "bucket", BUCKET);
    }

    @Test
    void uploadVideo_shouldCreateBucketWhenMissingAndUpload() throws Exception {
        when(minioClient.bucketExists(any())).thenReturn(false);
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "data".getBytes());

        String key = service.uploadVideo(file, "user@test.com");

        assertThat(key).startsWith("videos/user@test.com/").endsWith("_video.mp4");
    }

    @Test
    void uploadVideo_shouldSkipBucketCreationWhenAlreadyExists() throws Exception {
        when(minioClient.bucketExists(any())).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "data".getBytes());

        String key = service.uploadVideo(file, "user@test.com");

        assertThat(key).contains("user@test.com");
    }

    @Test
    void uploadVideo_shouldWrapExceptionOnFailure() throws Exception {
        when(minioClient.bucketExists(any())).thenThrow(new RuntimeException("MinIO down"));
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "data".getBytes());

        assertThatThrownBy(() -> service.uploadVideo(file, "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao fazer upload do vídeo");
    }

    @Test
    void downloadFile_shouldDelegateToMinioClient() throws Exception {
        when(minioClient.getObject(any())).thenReturn(null);

        assertThat(service.downloadFile("videos/key.mp4")).isNull();
    }

    @Test
    void downloadFile_shouldWrapExceptionOnFailure() throws Exception {
        when(minioClient.getObject(any())).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> service.downloadFile("videos/key.mp4"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao baixar arquivo");
    }
}
