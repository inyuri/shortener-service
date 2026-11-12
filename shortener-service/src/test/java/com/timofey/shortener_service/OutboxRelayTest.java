package com.timofey.shortener_service;

import com.timofey.shortener_service.model.OutboxEvent;
import com.timofey.shortener_service.model.OutboxStatus;
import com.timofey.shortener_service.repository.OutboxRepository;
import com.timofey.shortener_service.service.OutboxRelay;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    private static final String TOPIC = "link-clicks";
    private static final String SHORT_CODE = "abc12345";
    private static final String PAYLOAD = "{\"shortCode\":\"abc12345\"}";

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxRelay outboxRelay;

    private OutboxEvent event;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outboxRelay, "linkClickedTopic", TOPIC);
        event = new OutboxEvent(UUID.randomUUID(), "Link", SHORT_CODE, "LinkClicked", PAYLOAD);
        when(outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(event));
    }

    @Test
    void publishPending_whenKafkaAvailable_marksSent() {
        when(kafkaTemplate.send(TOPIC, SHORT_CODE, PAYLOAD))
                .thenReturn(CompletableFuture.completedFuture(null));

        outboxRelay.publishPending();

        verify(kafkaTemplate).send(TOPIC, SHORT_CODE, PAYLOAD);
        assertEquals(OutboxStatus.SENT, event.getStatus());
        assertNotNull(event.getSentAt());
    }

    @Test
    void publishPending_whenKafkaFails_keepsPending() {
        when(kafkaTemplate.send(TOPIC, SHORT_CODE, PAYLOAD))
                .thenReturn(CompletableFuture.failedFuture(new TimeoutException("Kafka unavailable")));

        outboxRelay.publishPending();

        verify(kafkaTemplate).send(TOPIC, SHORT_CODE, PAYLOAD);
        assertEquals(OutboxStatus.PENDING, event.getStatus());
        assertNull(event.getSentAt());
    }
}
