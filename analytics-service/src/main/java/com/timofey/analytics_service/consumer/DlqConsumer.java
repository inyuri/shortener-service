package com.timofey.analytics_service.consumer;

import com.timofey.analytics_service.event.LinkClickedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DlqConsumer {

    private final List<String> failedEvents = new CopyOnWriteArrayList<>();

    @KafkaListener(
            topics = "${kafka.topics.link-clicks-dlt}",
            groupId = "analytics-dlq-group"
    )
    public void consume(ConsumerRecord<String, Object> record) {
        String msg = record.value() != null ? record.value().toString() : "empty";
        failedEvents.add(msg);
    }

    public Map<String, Object> getFailedEvents() {
        return Map.of(
                "count", failedEvents.size(),
                "events", failedEvents
        );
    }
}
