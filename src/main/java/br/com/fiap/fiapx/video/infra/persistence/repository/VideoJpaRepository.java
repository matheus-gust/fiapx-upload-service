package br.com.fiap.fiapx.video.infra.persistence.repository;

import br.com.fiap.fiapx.video.infra.persistence.entity.VideoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VideoJpaRepository extends JpaRepository<VideoJpaEntity, UUID> {
    List<VideoJpaEntity> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
