package br.com.fiap.fiapx.video.application.dtos;

import br.com.fiap.fiapx.video.domain.model.VideoStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoResponseDTO(
        UUID id,
        String originalFilename,
        VideoStatus status,
        String downloadUrl,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
