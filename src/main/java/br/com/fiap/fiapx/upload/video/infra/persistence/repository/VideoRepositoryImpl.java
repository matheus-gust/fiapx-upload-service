package br.com.fiap.fiapx.upload.video.infra.persistence.repository;

import br.com.fiap.fiapx.upload.video.domain.model.Video;
import br.com.fiap.fiapx.upload.video.domain.repository.VideoRepository;
import br.com.fiap.fiapx.upload.video.infra.persistence.entity.VideoJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VideoRepositoryImpl implements VideoRepository {

    private final VideoJpaRepository jpaRepository;

    @Override
    public Video save(Video video) {
        VideoJpaEntity entity = VideoJpaEntity.builder()
                .id(video.id())
                .userId(video.userId())
                .originalFilename(video.originalFilename())
                .s3Key(video.s3Key())
                .zipS3Key(video.zipS3Key())
                .status(video.status())
                .errorMessage(video.errorMessage())
                .userEmail(video.userEmail())
                .build();
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Video> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Video> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    private Video toDomain(VideoJpaEntity e) {
        return new Video(e.getId(), e.getUserId(), e.getOriginalFilename(), e.getS3Key(),
                e.getZipS3Key(), e.getStatus(), e.getErrorMessage(), e.getUserEmail(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
