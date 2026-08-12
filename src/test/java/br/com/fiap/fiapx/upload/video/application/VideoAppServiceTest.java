package br.com.fiap.fiapx.upload.video.application;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoAppServiceTest {

    @Mock private VideoRepository videoRepository;
    @Mock private MinioStorageService storageService;
    @Mock private VideoEventPublisher eventPublisher;

    @InjectMocks private VideoAppService videoAppService;

    @Test
    void listByUser_returnsVideos() {
        UUID userId = UUID.randomUUID();
        Video v = new Video(UUID.randomUUID(), userId, "test.mp4", "key", null,
                VideoStatus.PENDING, null, "a@b.com", LocalDateTime.now(), LocalDateTime.now());
        when(videoRepository.findByUserId(userId)).thenReturn(List.of(v));

        var result = videoAppService.listByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).originalFilename()).isEqualTo("test.mp4");
    }

    @Test
    void getDownloadUrl_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(videoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoAppService.getDownloadUrl(id, UUID.randomUUID()))
                .isInstanceOf(VideoNotFoundException.class);
    }

    @Test
    void getDownloadUrl_throwsWhenNotDone() {
        UUID userId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Video v = new Video(videoId, userId, "test.mp4", "key", null,
                VideoStatus.PROCESSING, null, "a@b.com", LocalDateTime.now(), LocalDateTime.now());
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> videoAppService.getDownloadUrl(videoId, userId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getDownloadUrl_returnsUrlWhenDone() {
        UUID userId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        Video v = new Video(videoId, userId, "test.mp4", "key", "zip/result.zip",
                VideoStatus.DONE, null, "a@b.com", LocalDateTime.now(), LocalDateTime.now());
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(v));
        when(storageService.getPresignedUrl(any(), any())).thenReturn("http://minio/result.zip");

        String url = videoAppService.getDownloadUrl(videoId, userId);

        assertThat(url).isEqualTo("http://minio/result.zip");
    }
}
