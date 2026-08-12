package br.com.fiap.fiapx.upload.video.application;

import br.com.fiap.fiapx.upload.config.MinioConfig;
import br.com.fiap.fiapx.upload.video.application.dtos.VideoResponseDTO;
import br.com.fiap.fiapx.upload.video.application.dtos.VideoUploadedMessage;
import br.com.fiap.fiapx.upload.video.application.messaging.VideoEventPublisher;
import br.com.fiap.fiapx.upload.video.domain.model.Video;
import br.com.fiap.fiapx.upload.video.domain.repository.VideoRepository;
import br.com.fiap.fiapx.upload.video.domain.shared.VideoNotFoundException;
import br.com.fiap.fiapx.upload.video.domain.valueobjects.VideoStatus;
import br.com.fiap.fiapx.upload.video.infra.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoAppService {

    private final VideoRepository videoRepository;
    private final MinioStorageService storageService;
    private final VideoEventPublisher eventPublisher;

    public VideoResponseDTO upload(MultipartFile file, UUID userId, String userEmail) {
        String s3Key = "videos/" + userId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            storageService.uploadFile(MinioConfig.VIDEOS_BUCKET, s3Key,
                    file.getInputStream(), file.getSize(), file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload video", e);
        }
        Video video = new Video(UUID.randomUUID(), userId, file.getOriginalFilename(),
                s3Key, null, VideoStatus.PENDING, null, userEmail, null, null);
        Video saved = videoRepository.save(video);
        eventPublisher.publishVideoUploaded(
                new VideoUploadedMessage(saved.id(), saved.userId(), saved.s3Key(),
                        saved.originalFilename(), saved.userEmail()));
        return toDTO(saved);
    }

    public List<VideoResponseDTO> listByUser(UUID userId) {
        return videoRepository.findByUserId(userId).stream().map(this::toDTO).toList();
    }

    public String getDownloadUrl(UUID videoId, UUID userId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId));
        if (!video.userId().equals(userId)) {
            throw new VideoNotFoundException(videoId);
        }
        if (video.status() != VideoStatus.DONE || video.zipS3Key() == null) {
            throw new IllegalStateException("Video is not ready for download. Status: " + video.status());
        }
        return storageService.getPresignedUrl(MinioConfig.ZIPS_BUCKET, video.zipS3Key());
    }

    private VideoResponseDTO toDTO(Video v) {
        return new VideoResponseDTO(v.id(), v.originalFilename(), v.status(),
                v.errorMessage(), v.createdAt(), v.updatedAt());
    }
}
