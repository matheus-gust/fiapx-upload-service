package br.com.fiap.fiapx.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "fiapx.videos";
    public static final String DLX = "fiapx.videos.dlx";

    public static final String PROCESSING_QUEUE = "video.processing";
    public static final String PROCESSING_DLQ = "video.processing.dlq";
    public static final String NOTIFICATION_QUEUE = "video.notification";

    public static final String ROUTING_PROCESSING = "video.process";
    public static final String ROUTING_NOTIFICATION = "video.notify";

    @Bean
    TopicExchange videoExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    TopicExchange videoDlx() {
        return new TopicExchange(DLX, true, false);
    }

    @Bean
    Queue processingQueue() {
        return QueueBuilder.durable(PROCESSING_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", PROCESSING_DLQ)
                .build();
    }

    @Bean
    Queue processingDlq() {
        return QueueBuilder.durable(PROCESSING_DLQ).build();
    }

    @Bean
    Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    Binding processingBinding() {
        return BindingBuilder.bind(processingQueue()).to(videoExchange()).with(ROUTING_PROCESSING);
    }

    @Bean
    Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(videoExchange()).with(ROUTING_NOTIFICATION);
    }

    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
