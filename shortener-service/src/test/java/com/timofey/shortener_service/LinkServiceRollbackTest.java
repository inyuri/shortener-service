package com.timofey.shortener_service;

import com.timofey.shortener_service.model.Link;
import com.timofey.shortener_service.repository.LinkRepository;
import com.timofey.shortener_service.repository.OutboxRepository;
import com.timofey.shortener_service.service.LinkService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ContextConfiguration(classes = LinkServiceRollbackTest.JpaConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class LinkServiceRollbackTest {

    @Autowired
    private LinkService linkService;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private MeterRegistry meterRegistry;

    @MockitoBean
    private WebClient analyticsWebClient;

    @Test
    void redirect_whenCommitFails_rollsBackClicksAndOutbox() {
        Link link = linkRepository.save(new Link("abc12345", "https://example.com", 7, null));
        var failure = new IllegalStateException("Simulated failure before commit");

        when(meterRegistry.counter("links.clicks")).thenAnswer(invocation -> {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCommit(boolean readOnly) {
                    entityManager.flush();
                    entityManager.clear();
                    assertEquals(8, linkRepository.findById(link.getId()).orElseThrow().getClicks());
                    assertEquals(1, outboxRepository.count());
                    throw failure;
                }
            });
            return mock(Counter.class);
        });

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> linkService.redirect(link.getShortCode(), "test-agent")));

        assertEquals(7, linkRepository.findById(link.getId()).orElseThrow().getClicks());
        assertEquals(0, outboxRepository.count());
    }

    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = Link.class)
    @EnableJpaRepositories(basePackageClasses = LinkRepository.class)
    @Import(LinkService.class)
    static class JpaConfig {

        @Bean
        JsonMapper jsonMapper() {
            return JsonMapper.builder().build();
        }
    }
}
