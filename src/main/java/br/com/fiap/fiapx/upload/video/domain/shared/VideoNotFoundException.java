package br.com.fiap.fiapx.upload.video.domain.shared;

import java.util.UUID;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(UUID id) {
        super("Video not found: " + id);
    }
}
