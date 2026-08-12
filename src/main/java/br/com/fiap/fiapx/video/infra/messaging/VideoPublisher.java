package br.com.fiap.fiapx.video.infra.messaging;

import br.com.fiap.fiapx.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishProcessing(UUID videoId, String s3Key, String userEmail) {
        VideoProcessingMessage message = new VideoProcessingMessage(videoId, s3Key, userEmail);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_PROCESSING, message);
        log.info("Published video for processing: {}", videoId);
    }

    public record VideoProcessingMessage(UUID videoId, String s3Key, String userEmail) {}
}
