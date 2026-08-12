package br.com.fiap.fiapx.upload.video.application.messaging;

import br.com.fiap.fiapx.upload.config.RabbitConfig;
import br.com.fiap.fiapx.upload.video.application.dtos.VideoUploadedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishVideoUploaded(VideoUploadedMessage message) {
        log.info("Publishing video.processing for videoId={}", message.videoId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.VIDEO_PROCESSING_QUEUE, message);
    }
}
