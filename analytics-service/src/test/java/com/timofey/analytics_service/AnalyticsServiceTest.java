package com.timofey.analytics_service;

import com.timofey.analytics_service.dto.AnalyticsResponse;
import com.timofey.analytics_service.entity.ClickEvent;
import com.timofey.analytics_service.event.LinkClickedEvent;
import com.timofey.analytics_service.repository.ClickEventRepository;
import com.timofey.analytics_service.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

	@Mock
	private ClickEventRepository clickEventRepository;

	@InjectMocks
	private AnalyticsService analyticsService;

	@Test
	void process_savesEventToRepository() {
		LinkClickedEvent event =
				new LinkClickedEvent(
						"abc123",
						"https://google.com",
						LocalDateTime.now(),
						"Chrome",
						UUID.randomUUID()
				);

		analyticsService.process(event);

		ArgumentCaptor<ClickEvent> captor =
				ArgumentCaptor.forClass(ClickEvent.class);

		verify(clickEventRepository)
				.save(captor.capture());

		ClickEvent saved =
				captor.getValue();

		assertEquals(
				"abc123",
				saved.getShortCode()
		);

		assertEquals(
				"https://google.com",
				saved.getOriginalUrl()
		);
	}

	@Test
	void process_twoEventsWithSameShortCode_savesBothEvents() {

		LinkClickedEvent first =
				new LinkClickedEvent(
						"abc123",
						"https://google.com",
						LocalDateTime.now(),
						"Chrome",
						UUID.randomUUID()
				);

		LinkClickedEvent second =
				new LinkClickedEvent(
						"abc123",
						"https://google.com",
						LocalDateTime.now(),
						"Firefox",
						UUID.randomUUID()
				);

		analyticsService.process(first);
		analyticsService.process(second);

		verify(clickEventRepository, times(2))
				.save(any(ClickEvent.class));
	}

	@Test
	void getAnalytics_returnsClickCounts() {
		when(clickEventRepository.countByShortCode("abc123")).thenReturn(10);

		when(clickEventRepository.countTodayClicks(
						eq("abc123"),
						any(LocalDateTime.class)
				)
		).thenReturn(3);

		AnalyticsResponse response =
				analyticsService.getAnalytics("abc123");

		assertEquals(
				"abc123",
				response.getShortCode()
		);

		assertEquals(
				10,
				response.getTotalClicks()
		);

		assertEquals(
				3,
				response.getTodayClicks()
		);
	}
}
