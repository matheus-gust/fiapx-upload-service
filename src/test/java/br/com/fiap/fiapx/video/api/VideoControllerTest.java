package br.com.fiap.fiapx.video.api;

import br.com.fiap.fiapx.video.application.VideoService;
import br.com.fiap.fiapx.video.application.dtos.VideoResponseDTO;
import br.com.fiap.fiapx.video.domain.model.VideoStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
class VideoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean VideoService videoService;
    @MockBean br.com.fiap.fiapx.video.infra.security.JwtAuthFilter jwtAuthFilter;

    private VideoResponseDTO buildResponse(VideoStatus status) {
        return new VideoResponseDTO(UUID.randomUUID(), "video.mp4", status, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void upload_shouldReturn202() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "data".getBytes());
        when(videoService.upload(any(), eq("user@test.com"))).thenReturn(buildResponse(VideoStatus.PENDING));

        mockMvc.perform(multipart("/videos").file(file).with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void list_shouldReturnUserVideos() throws Exception {
        when(videoService.listByUser("user@test.com"))
                .thenReturn(List.of(buildResponse(VideoStatus.DONE)));

        mockMvc.perform(get("/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("DONE"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getById_shouldReturnVideo() throws Exception {
        UUID id = UUID.randomUUID();
        when(videoService.getById(eq(id), eq("user@test.com"))).thenReturn(buildResponse(VideoStatus.PROCESSING));

        mockMvc.perform(get("/videos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void upload_shouldReturn401WhenNotAuthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "data".getBytes());

        mockMvc.perform(multipart("/videos").file(file))
                .andExpect(status().isUnauthorized());
    }
}
