package br.com.fiap.fiapx.video.application;

import br.com.fiap.fiapx.video.application.dtos.VideoResponseDTO;
import br.com.fiap.fiapx.video.domain.model.Video;
import br.com.fiap.fiapx.video.domain.model.VideoStatus;
import br.com.fiap.fiapx.video.domain.repository.VideoRepository;
import br.com.fiap.fiapx.video.infra.messaging.VideoPublisher;
import br.com.fiap.fiapx.video.infra.storage.MinioStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock private VideoRepository videoRepository;
    @Mock private MinioStorageService storageService;
    @Mock private VideoPublisher videoPublisher;

    @InjectMocks
    private VideoService videoService;

    private UUID videoId;
    private Video pendingVideo;

    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        pendingVideo = Video.builder()
                .id(videoId).userEmail("user@test.com")
                .originalFilename("video.mp4").s3Key("videos/user@test.com/video.mp4")
                .status(VideoStatus.PENDING).build();
    }

    @Test
    void upload_shouldSaveVideoAndPublishMessage() {
        MultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "data".getBytes());
        when(storageService.uploadVideo(any(), eq("user@test.com"))).thenReturn("videos/user@test.com/video.mp4");
        when(videoRepository.save(any())).thenReturn(pendingVideo);

        VideoResponseDTO response = videoService.upload(file, "user@test.com");

        assertThat(response.originalFilename()).isEqualTo("video.mp4");
        assertThat(response.status()).isEqualTo(VideoStatus.PENDING);
        verify(videoPublisher).publishProcessing(eq(videoId), any(), eq("user@test.com"));
    }

    @Test
    void listByUser_shouldReturnUserVideos() {
        Video doneVideo = Video.builder()
                .id(videoId).userEmail("user@test.com")
                .originalFilename("video.mp4").s3Key("key").zipS3Key("zip-key")
                .status(VideoStatus.DONE).build();
        when(videoRepository.findByUserEmail("user@test.com")).thenReturn(List.of(doneVideo));
        when(storageService.getPresignedDownloadUrl("zip-key")).thenReturn("http://minio/download");

        List<VideoResponseDTO> result = videoService.listByUser("user@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(VideoStatus.DONE);
        assertThat(result.get(0).downloadUrl()).isEqualTo("http://minio/download");
    }

    @Test
    void getById_shouldReturnVideoOfAuthenticatedUser() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(pendingVideo));

        VideoResponseDTO response = videoService.getById(videoId, "user@test.com");

        assertThat(response.id()).isEqualTo(videoId);
        assertThat(response.status()).isEqualTo(VideoStatus.PENDING);
    }

    @Test
    void getById_shouldThrowWhenVideoNotBelongsToUser() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(pendingVideo));

        assertThatThrownBy(() -> videoService.getById(videoId, "outro@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void listByUser_shouldReturnEmptyWhenNoVideos() {
        when(videoRepository.findByUserEmail("vazio@test.com")).thenReturn(List.of());

        List<VideoResponseDTO> result = videoService.listByUser("vazio@test.com");

        assertThat(result).isEmpty();
    }
}
