package br.com.fiap.fiapx.bdd;

import br.com.fiap.fiapx.video.application.VideoService;
import br.com.fiap.fiapx.video.application.dtos.VideoResponseDTO;
import br.com.fiap.fiapx.video.domain.model.Video;
import br.com.fiap.fiapx.video.domain.model.VideoStatus;
import br.com.fiap.fiapx.video.domain.repository.VideoRepository;
import br.com.fiap.fiapx.video.infra.messaging.VideoPublisher;
import br.com.fiap.fiapx.video.infra.storage.MinioStorageService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VideoSteps {

    private final VideoRepository videoRepository = Mockito.mock(VideoRepository.class);
    private final MinioStorageService storageService = Mockito.mock(MinioStorageService.class);
    private final VideoPublisher videoPublisher = Mockito.mock(VideoPublisher.class);
    private final VideoService videoService = new VideoService(videoRepository, storageService, videoPublisher);

    private VideoResponseDTO lastResponse;
    private List<VideoResponseDTO> lastList;
    private UUID knownVideoId;
    private String currentUser;

    @Given("que o usuario {string} esta autenticado")
    public void usuarioAutenticado(String email) {
        currentUser = email;
    }

    @When("o usuario envia um arquivo de video {string}")
    public void enviaVideo(String filename) {
        MultipartFile file = new MockMultipartFile("file", filename, "video/mp4", "data".getBytes());
        UUID id = UUID.randomUUID();
        Video saved = Video.builder().id(id).userEmail(currentUser).originalFilename(filename)
                .s3Key("key").status(VideoStatus.PENDING).build();
        when(storageService.uploadVideo(any(), eq(currentUser))).thenReturn("key");
        when(videoRepository.save(any())).thenReturn(saved);
        lastResponse = videoService.upload(file, currentUser);
        knownVideoId = id;
    }

    @Then("o sistema registra o video com status {string}")
    public void verificaStatus(String status) {
        assertThat(lastResponse.status()).isEqualTo(VideoStatus.valueOf(status));
    }

    @Then("publica mensagem de processamento na fila")
    public void verificaPublicacao() {
        verify(videoPublisher).publishProcessing(any(), any(), eq(currentUser));
    }

    @Given("que o usuario {string} possui {int} videos cadastrados")
    public void possuiVideos(String email, int count) {
        currentUser = email;
        UUID id1 = UUID.randomUUID(), id2 = UUID.randomUUID();
        Video v1 = Video.builder().id(id1).userEmail(email).originalFilename("v1.mp4")
                .s3Key("k1").status(VideoStatus.DONE).zipS3Key("z1").build();
        Video v2 = Video.builder().id(id2).userEmail(email).originalFilename("v2.mp4")
                .s3Key("k2").status(VideoStatus.PENDING).build();
        when(videoRepository.findByUserEmail(email)).thenReturn(List.of(v1, v2));
        when(storageService.getPresignedDownloadUrl("z1")).thenReturn("http://url");
    }

    @When("o usuario solicita a listagem de seus videos")
    public void listagem() {
        lastList = videoService.listByUser(currentUser);
    }

    @Then("o sistema retorna {int} videos")
    public void retornaVideos(int count) {
        assertThat(lastList).hasSize(count);
    }

    @Given("que existe um video com id conhecido do usuario {string}")
    public void videoComId(String email) {
        currentUser = email;
        knownVideoId = UUID.randomUUID();
        Video video = Video.builder().id(knownVideoId).userEmail(email).originalFilename("v.mp4")
                .s3Key("key").status(VideoStatus.PROCESSING).build();
        when(videoRepository.findById(knownVideoId)).thenReturn(Optional.of(video));
    }

    @When("o usuario consulta o video por id")
    public void consultaVideo() {
        lastResponse = videoService.getById(knownVideoId, currentUser);
    }

    @Then("o sistema retorna os dados do video")
    public void retornaDadosVideo() {
        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.id()).isEqualTo(knownVideoId);
    }
}
