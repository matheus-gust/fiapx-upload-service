package br.com.fiap.fiapx.video.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@With
public class Video {
    private UUID id;
    private String userEmail;
    private String originalFilename;
    private String s3Key;
    private String zipS3Key;
    private VideoStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
