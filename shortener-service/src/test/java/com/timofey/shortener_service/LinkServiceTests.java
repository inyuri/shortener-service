package com.timofey.shortener_service;

import com.timofey.shortener_service.dto.LinkResponse;
import com.timofey.shortener_service.exception.LinkNotFoundException;
import com.timofey.shortener_service.model.Link;
import com.timofey.shortener_service.repository.LinkRepository;
import com.timofey.shortener_service.service.LinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkServiceTests {

	@Mock
	private LinkRepository linkRepository;

	@InjectMocks
	private LinkService linkService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(linkService, "baseUrl", "http://localhost:8080");
	}

	@Test
	void createShouldSaveAndReturnResponse() {
		LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

		when(linkRepository.save(any(Link.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Map<String, Object> response = linkService.create(
			"https://google.com",
			expiresAt
		);

		assertNotNull(response.get("shortCode"));
		assertEquals(
				"http://localhost:8080/" + response.get("shortCode"),
				response.get("shortUrl")
		);

		verify(linkRepository).save(any(Link.class));
	}

	@Test
	void getShouldReturnLinkWhenShortCodeExists() {
		Link link = new Link(
				"abc12345",
				"https://google.com",
				10,
				LocalDateTime.now().plusDays(1)
		);

		when(linkRepository.getLinkByShortCode("abc12345"))
				.thenReturn(Optional.of(link));

		LinkResponse response = linkService.get("abc12345");

		assertEquals("abc12345", response.getShortCode());
		assertEquals("https://google.com", response.getOriginalUrl());
		assertEquals(10, response.getClicks());

		verify(linkRepository).getLinkByShortCode("abc12345");
	}

	@Test
	void getShouldReturnExceptionWhenShortCodeDoesNotExists() {
		when(linkRepository.getLinkByShortCode("unknown"))
				.thenReturn(Optional.empty());

		assertThrows(
				LinkNotFoundException.class,
				() -> linkService.get("unknown")
		);

		verify(linkRepository).getLinkByShortCode("unknown");
	}

}
