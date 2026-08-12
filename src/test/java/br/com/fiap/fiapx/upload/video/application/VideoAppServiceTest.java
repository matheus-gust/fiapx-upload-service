package br.com.fiap.fiapx.upload.video.application;

import br.com.fiap.fiapx.upload.video.application.dtos.VideoResponseDTO;
import br.com.fiap.fiapx.upload.video.application.messaging.VideoEventPublisher;
import br.com.fiap.fiapx.upload.video.domain.model.Video;
import br.com.fiap.fiapx.upload.video.domain.repository.VideoRepository;
import br.com.fiap.fiapx.upload.video.domain.shared.VideoNotFoundException;
import br.com.fiap.fiapx.upload.video.domain.valueobjects.VideoStatus;
import br.com.fiap.fiapx.upload.video.infra.storage.MinioStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoAppServiceTest {

    @Mock private VideoRepository videoRepository;
    @Mock private MinioStorageService storageService;
    @Mock private VideoEventPublisher eventPublisher;

    @InjectMocks private VideoAppService videoAppService;

    private Video makeVideo(UUID userId, VideoStatus status, String zipKey) {
        return new Video(UUID.randomUUID(), userId, "test.mp4", "s3/key", zipKey,
                status, null, "user@test.com", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void upload_savesVideoAndPublishesEvent() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", new byte[100]);
        Video saved = makeVideo(userId, VideoStatus.PENDING, null);
        when(videoRepository.save(any())).thenReturn(saved);
        doNothing().when(storageService).uploadFile(any(), any(), any(), anyLong(), any());

        VideoResponseDTO result = videoAppService.upload(file, userId, "user@test.com");

        assertThat(result.originalFilename()).isEqualTo("test.mp4");
        assertThat(result.status()).isEqualTo(VideoStatus.PENDING);
        verify(storageService).uploadFile(any(), any(), any(), anyLong(), any());
        verify(eventPublisher).publishVideoUploaded(any());
    }

    @Test
    void upload_throwsWhenStorageFails() {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", new byte[100]);
        doThrow(new RuntimeException("MinIO down")).when(storageService).uploadFile(any(), any(), any(), anyLong(), any());

        assertThatThrownBy(() -> videoAppService.upload(file, userId, "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("upload");
    }

    @Test
    void listByUser_returnsVideos() {
        UUID userId = UUID.randomUUID();
        when(videoRepository.findByUserId(userId)).thenReturn(List.of(makeVideo(userId, VideoStatus.PENDING, null)));

        List<VideoResponseDTO> result = videoAppService.listByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).originalFilename()).isEqualTo("test.mp4");
    }

    @Test
    void listByUser_returnsEmptyList() {
        UUID userId = UUID.randomUUID();
        when(videoRepository.findByUserId(userId)).thenReturn(List.of());

        assertThat(videoAppService.listByUser(userId)).isEmpty();
    }

    @Test
    void getDownloadUrl_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(videoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoAppService.getDownloadUrl(id, UUID.randomUUID()))
                .isInstanceOf(VideoNotFoundException.class);
    }

    @Test
    void getDownloadUrl_throwsWhenWrongUser() {
        UUID videoId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Video v = makeVideo(ownerId, VideoStatus.DONE, "zip/result.zip");
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> videoAppService.getDownloadUrl(videoId, otherId))
                .isInstanceOf(VideoNotFoundException.class);
    }

    @Test
    void getDownloadUrl_throwsWhenNotDone() {
        UUID userId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Video v = makeVideo(userId, VideoStatus.PROCESSING, null);
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> videoAppService.getDownloadUrl(videoId, userId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getDownloadUrl_returnsUrlWhenDone() {
        UUID userId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Video v = makeVideo(userId, VideoStatus.DONE, "zip/result.zip");
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(v));
        when(storageService.getPresignedUrl(any(), any())).thenReturn("http://minio/result.zip");

        String url = videoAppService.getDownloadUrl(videoId, userId);

        assertThat(url).isEqualTo("http://minio/result.zip");
    }
}
