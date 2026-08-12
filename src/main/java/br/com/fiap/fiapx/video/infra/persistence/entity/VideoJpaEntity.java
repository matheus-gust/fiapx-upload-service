package br.com.fiap.fiapx.video.infra.persistence.entity;

import br.com.fiap.fiapx.video.domain.model.VideoStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "videos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "zip_s3_key")
    private String zipS3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
