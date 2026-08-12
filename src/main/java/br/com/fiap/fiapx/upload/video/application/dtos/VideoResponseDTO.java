package br.com.fiap.fiapx.upload.video.application.dtos;

import br.com.fiap.fiapx.upload.video.domain.valueobjects.VideoStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoResponseDTO(
        UUID id,
        String originalFilename,
        VideoStatus status,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
