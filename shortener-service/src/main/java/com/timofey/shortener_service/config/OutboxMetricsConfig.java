package com.timofey.shortener_service.config;

import com.timofey.shortener_service.model.OutboxStatus;
import com.timofey.shortener_service.repository.OutboxRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OutboxMetricsConfig {

    @Bean
    MeterBinder outboxMetrics(OutboxRepository repository) {
        return registry -> Gauge.builder(
                        "outbox.pending.count",
                        repository,
                        repo -> repo.countByStatus(OutboxStatus.PENDING))
                .description("Number of outbox events waiting to be published")
                .register(registry);
    }
}