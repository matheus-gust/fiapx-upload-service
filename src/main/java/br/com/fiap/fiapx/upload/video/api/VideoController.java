package br.com.fiap.fiapx.upload.video.api;

import br.com.fiap.fiapx.upload.video.application.VideoAppService;
import br.com.fiap.fiapx.upload.video.application.dtos.VideoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Videos", description = "Upload e status de videos")
public class VideoController {

    private final VideoAppService videoAppService;

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Enviar video para processamento")
    public VideoResponseDTO upload(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        String userEmail = (String) auth.getCredentials();
        return videoAppService.upload(file, userId, userEmail);
    }

    @GetMapping
    @Operation(summary = "Listar status dos videos do usuario")
    public List<VideoResponseDTO> list(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return videoAppService.listByUser(userId);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Obter URL de download do zip")
    public java.util.Map<String, String> download(@PathVariable UUID id, Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        String url = videoAppService.getDownloadUrl(id, userId);
        return java.util.Map.of("url", url);
    }
}
