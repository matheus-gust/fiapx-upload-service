package br.com.fiap.fiapx.upload.video.domain.model;

import br.com.fiap.fiapx.upload.video.domain.valueobjects.VideoStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record Video(
        UUID id,
        UUID userId,
        String originalFilename,
        String s3Key,
        String zipS3Key,
        VideoStatus status,
        String errorMessage,
        String userEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
