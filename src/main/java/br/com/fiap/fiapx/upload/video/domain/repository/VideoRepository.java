package br.com.fiap.fiapx.upload.video.domain.repository;

import br.com.fiap.fiapx.upload.video.domain.model.Video;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepository {
    Video save(Video video);
    Optional<Video> findById(UUID id);
    List<Video> findByUserId(UUID userId);
}
