package com.timofey.analytics_service.consumer;

import com.timofey.analytics_service.event.LinkClickedEvent;
import com.timofey.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class ClickEventConsumer {

    private final AnalyticsService analyticsService;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "${kafka.topics.link-clicks}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(byte[] payload) {
        analyticsService.process(jsonMapper.readValue(payload, LinkClickedEvent.class));
    }

}
