package com.timofey.shortener_service.service;

import com.timofey.shortener_service.dto.AnalyticsResponse;
import com.timofey.shortener_service.dto.LinkResponse;
import com.timofey.shortener_service.event.LinkClickedEvent;
import com.timofey.shortener_service.exception.LinkExpiredException;
import com.timofey.shortener_service.exception.LinkNotFoundException;
import com.timofey.shortener_service.model.Link;
import com.timofey.shortener_service.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final KafkaTemplate<String, LinkClickedEvent> kafkaTemplate;
    private final WebClient analyticsWebClient;

    @Value("${kafka.topics.link-clicks}")
    private String linkClickedTopic;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public Map<String, Object> create(String originalUrl, LocalDateTime expiresAt) {
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("expiration date must be in the future");
        }

        String shortCode = UUID.randomUUID().toString().substring(0, 8);


        Link link = new Link(shortCode, originalUrl, 0, expiresAt);
        Link savedLink = linkRepository.save(link);

        return Map.of(
                "shortCode", savedLink.getShortCode(),
                "shortUrl", baseUrl + "/" + savedLink.getShortCode()
        );
    }

    @Transactional
    public String redirect(String shortCode, String userAgent) {
        Link link = linkRepository.getLinkByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new LinkExpiredException(shortCode);
        }

        link.setClicks(link.getClicks() + 1);
        linkRepository.save(link);

        LinkClickedEvent event = new LinkClickedEvent(
                link.getShortCode(),
                link.getOriginalUrl(),
                LocalDateTime.now(),
                userAgent,
                UUID.randomUUID()
        );

        kafkaTemplate.send(
                linkClickedTopic,
                link.getShortCode(),
                event
        );
        
        return link.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "links", key = "#p0")
    public LinkResponse get(String shortCode) {
        Link savedLink = linkRepository.getLinkByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        return new LinkResponse(
                savedLink.getShortCode(),
                savedLink.getOriginalUrl(),
                savedLink.getClicks(),
                savedLink.getCreatedAt(),
                savedLink.getExpiresAt()
        );
    }

    @Transactional
    @CacheEvict(value = "links", key = "#p0")
    public void delete(String shortCode) {
        Link link = linkRepository.getLinkByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        linkRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats(String shortCode) {
        Link link = linkRepository.getLinkByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        return Map.of(
          "clicks", link.getClicks()
        );
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        linkRepository.getLinkByShortCode(shortCode)
                .orElseThrow(() ->
                        new LinkNotFoundException("Link not found")
                );

        return analyticsWebClient
                .get()
                .uri("/api/analytics/{shortCode}", shortCode)
                .retrieve()
                .bodyToMono(AnalyticsResponse.class)
                .onErrorReturn(
                        new AnalyticsResponse(
                                shortCode,
                                0,
                                0
                        )
                )
                .block();
    }
}
