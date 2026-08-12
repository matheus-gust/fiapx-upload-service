package br.com.fiap.fiapx.upload.video.application.dtos;

import java.util.UUID;

public record VideoUploadedMessage(
        UUID videoId,
        UUID userId,
        String s3Key,
        String originalFilename,
        String userEmail
) {}
