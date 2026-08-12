package br.com.fiap.fiapx.video.infra.persistence.repository;

import br.com.fiap.fiapx.video.domain.model.Video;
import br.com.fiap.fiapx.video.domain.repository.VideoRepository;
import br.com.fiap.fiapx.video.infra.persistence.entity.VideoJpaEntity;
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
        VideoJpaEntity entity = toEntity(video);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Video> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Video> findByUserEmail(String userEmail) {
        return jpaRepository.findByUserEmailOrderByCreatedAtDesc(userEmail).stream().map(this::toDomain).toList();
    }

    private VideoJpaEntity toEntity(Video v) {
        return VideoJpaEntity.builder()
                .id(v.getId()).userEmail(v.getUserEmail()).originalFilename(v.getOriginalFilename())
                .s3Key(v.getS3Key()).zipS3Key(v.getZipS3Key()).status(v.getStatus())
                .errorMessage(v.getErrorMessage()).build();
    }

    private Video toDomain(VideoJpaEntity e) {
        return Video.builder()
                .id(e.getId()).userEmail(e.getUserEmail()).originalFilename(e.getOriginalFilename())
                .s3Key(e.getS3Key()).zipS3Key(e.getZipS3Key()).status(e.getStatus())
                .errorMessage(e.getErrorMessage()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
