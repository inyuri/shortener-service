package com.timofey.shortener_service.service;

import com.timofey.shortener_service.model.OutboxEvent;
import com.timofey.shortener_service.model.OutboxStatus;
import com.timofey.shortener_service.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.link-clicks}")
    private String linkClickedTopic;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> events = outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        log.debug("Outbox relay picked {} pending events", events.size());

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                        topicFor(event.getEventType()),
                        event.getAggregateId(),
                        event.getPayload()
                ).get();

                event.markSent();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Outbox relay interrupted, stopping batch");
                return;
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}, type={}: {}",
                        event.getId(), event.getEventType(), e.getMessage(), e);
            }
        }
    }

    private String topicFor(String eventType) {
        if (eventType.equals("LinkClicked")) {
            return linkClickedTopic;
        }
        else {
            throw new  IllegalStateException("Unknown event type: " + eventType);
        }
    }
}
