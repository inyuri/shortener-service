package com.timofey.shortener_service.controller;

import com.timofey.shortener_service.dto.AnalyticsResponse;
import com.timofey.shortener_service.dto.CreateLinkRequest;
import com.timofey.shortener_service.dto.LinkResponse;
import com.timofey.shortener_service.service.LinkService;
import com.timofey.shortener_service.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;
    private final RateLimitService rateLimitService;

    @PostMapping("/api/links")
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody CreateLinkRequest linkRequest,
            HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();

        if (!rateLimitService.isAllowed(ip)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Too many requests"
                    ));
        }

        Map<String, Object> response = linkService.create(
                linkRequest.getOriginalUrl(),
                linkRequest.getExpiresAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request
    ) {
        String originalUrl = linkService.redirect(shortCode, request.getHeader("User-Agent"));

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/api/links/{shortCode}")
    public ResponseEntity<LinkResponse> get(@PathVariable String shortCode) {
        LinkResponse linkResponse = linkService.get(shortCode);

        return ResponseEntity.ok(linkResponse);
    }

    @DeleteMapping("/api/links/{shortCode}")
    public ResponseEntity<Void> delete(@PathVariable String shortCode) {
        linkService.delete(shortCode);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/api/links/{shortCode}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable String shortCode) {
        Map<String, Object> stats = linkService.getStats(shortCode);

        return ResponseEntity
                .ok(stats);
    }

    @GetMapping("/api/links/{shortCode}/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        AnalyticsResponse response = linkService.getAnalytics(shortCode);

        return ResponseEntity.ok(response);
    }
}
