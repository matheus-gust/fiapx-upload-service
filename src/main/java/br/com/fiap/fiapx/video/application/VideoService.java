package br.com.fiap.fiapx.video.application;

import br.com.fiap.fiapx.video.application.dtos.VideoResponseDTO;
import br.com.fiap.fiapx.video.domain.model.Video;
import br.com.fiap.fiapx.video.domain.model.VideoStatus;
import br.com.fiap.fiapx.video.domain.repository.VideoRepository;
import br.com.fiap.fiapx.video.infra.messaging.VideoPublisher;
import br.com.fiap.fiapx.video.infra.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final MinioStorageService storageService;
    private final VideoPublisher videoPublisher;

    public VideoResponseDTO upload(MultipartFile file, String userEmail) {
        String s3Key = storageService.uploadVideo(file, userEmail);
        Video video = Video.builder()
                .userEmail(userEmail)
                .originalFilename(file.getOriginalFilename())
                .s3Key(s3Key)
                .status(VideoStatus.PENDING)
                .build();
        Video saved = videoRepository.save(video);
        videoPublisher.publishProcessing(saved.getId(), s3Key, userEmail);
        log.info("Video uploaded and queued: {}", saved.getId());
        return toDTO(saved);
    }

    public List<VideoResponseDTO> listByUser(String userEmail) {
        return videoRepository.findByUserEmail(userEmail).stream().map(this::toDTO).toList();
    }

    public VideoResponseDTO getById(UUID id, String userEmail) {
        Video video = videoRepository.findById(id)
                .filter(v -> v.getUserEmail().equals(userEmail))
                .orElseThrow(() -> new IllegalArgumentException("Vídeo não encontrado"));
        return toDTO(video);
    }

    private VideoResponseDTO toDTO(Video v) {
        String downloadUrl = null;
        if (v.getStatus() == VideoStatus.DONE && v.getZipS3Key() != null) {
            downloadUrl = storageService.getPresignedDownloadUrl(v.getZipS3Key());
        }
        return new VideoResponseDTO(v.getId(), v.getOriginalFilename(), v.getStatus(),
                downloadUrl, v.getErrorMessage(), v.getCreatedAt(), v.getUpdatedAt());
    }
}
