package br.com.fiap.fiapx.video.infra.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VideoPublisherTest {

    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks VideoPublisher videoPublisher;

    @Test
    void publishProcessing_shouldSendMessageToExchange() {
        UUID videoId = UUID.randomUUID();

        videoPublisher.publishProcessing(videoId, "videos/user/video.mp4", "user@test.com");

        verify(rabbitTemplate).convertAndSend(
                eq("fiapx.videos"),
                eq("video.process"),
                any(VideoPublisher.VideoProcessingMessage.class)
        );
    }
}
