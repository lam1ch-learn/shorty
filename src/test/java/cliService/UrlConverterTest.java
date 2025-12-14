package cliService;

import models.UrlData;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.RedisStore;

import static org.junit.jupiter.api.Assertions.*;

class UrlConverterTest {
	private RedisStore storage;
	private UrlConverter converter;
	private User user;

	@BeforeEach
	void setUp() {
		storage = new RedisStore();
		converter = new UrlConverter(storage);
		user = new User();
		// Очистка
		storage.listUrls().forEach(u -> storage.deleteUrl(u.getId().toString()));
	}

	@Test
	void generatesUniqueShortUrl() {
		UrlData url1 = new UrlData("https://example.com", "", user, -1, -1L);
		String short1 = converter.generateShortUrl(url1);

		UrlData url2 = new UrlData("https://google.com", "", user, -1, -1L);
		String short2 = converter.generateShortUrl(url2);

		assertNotEquals(short1, short2);
		assertTrue(short1.startsWith("short.com/"));
		assertTrue(short2.startsWith("short.com/"));
	}

	@Test
	void savesUrlWithGeneratedShortUrl() {
		UrlData url = new UrlData("https://test.com", "", user, -1, -1L);
		String shortUrl = converter.generateShortUrl(url);

		UrlData fromStorage = storage.findByShortUrl(shortUrl);
		assertNotNull(fromStorage);
		assertEquals("https://test.com", fromStorage.getFullUrl());
	}
}
