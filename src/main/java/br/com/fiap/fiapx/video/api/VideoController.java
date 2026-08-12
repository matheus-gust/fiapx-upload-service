package br.com.fiap.fiapx.video.api;

import br.com.fiap.fiapx.video.application.VideoService;
import br.com.fiap.fiapx.video.application.dtos.VideoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Videos", description = "Upload e acompanhamento de vídeos")
public class VideoController {

    private final VideoService videoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Envia vídeo para processamento")
    public VideoResponseDTO upload(@RequestParam("file") MultipartFile file, Authentication auth) {
        return videoService.upload(file, auth.getName());
    }

    @GetMapping
    @Operation(summary = "Lista todos os vídeos do usuário autenticado")
    public List<VideoResponseDTO> list(Authentication auth) {
        return videoService.listByUser(auth.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta status de um vídeo específico")
    public VideoResponseDTO getById(@PathVariable UUID id, Authentication auth) {
        return videoService.getById(id, auth.getName());
    }
}
