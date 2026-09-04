package com.timofey.analytics_service.consumer;

import com.timofey.analytics_service.config.KafkaConfig;
import com.timofey.analytics_service.event.LinkClickedEvent;
import com.timofey.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClickEventConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "${kafka.topics.link-clicks}",
    containerFactory = "kafkaListenerContainerFactory")
    public void consume(LinkClickedEvent event) {
        throw new RuntimeException("test");
        // analyticsService.process(event);
    }

}
