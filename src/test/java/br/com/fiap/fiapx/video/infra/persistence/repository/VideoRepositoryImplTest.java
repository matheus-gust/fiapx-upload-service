package br.com.fiap.fiapx.video.infra.persistence.repository;

import br.com.fiap.fiapx.video.domain.model.Video;
import br.com.fiap.fiapx.video.domain.model.VideoStatus;
import br.com.fiap.fiapx.video.infra.persistence.entity.VideoJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoRepositoryImplTest {

    @Mock
    VideoJpaRepository jpaRepository;

    VideoRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new VideoRepositoryImpl(jpaRepository);
    }

    private VideoJpaEntity buildEntity(UUID id) {
        return VideoJpaEntity.builder()
                .id(id).userEmail("user@test.com").originalFilename("video.mp4")
                .s3Key("videos/user@test.com/x.mp4").status(VideoStatus.PENDING)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_shouldPersistAndReturnDomainVideo() {
        UUID id = UUID.randomUUID();
        Video video = Video.builder()
                .userEmail("user@test.com").originalFilename("video.mp4")
                .s3Key("videos/user@test.com/x.mp4").status(VideoStatus.PENDING).build();
        when(jpaRepository.save(any())).thenReturn(buildEntity(id));

        Video result = repository.save(video);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getStatus()).isEqualTo(VideoStatus.PENDING);
    }

    @Test
    void findById_shouldReturnVideoWhenFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(buildEntity(id)));

        Optional<Video> result = repository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void findByUserEmail_shouldReturnVideosOrderedByCreatedAt() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findByUserEmailOrderByCreatedAtDesc("user@test.com"))
                .thenReturn(List.of(buildEntity(id)));

        List<Video> result = repository.findByUserEmail("user@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(id);
    }
}
