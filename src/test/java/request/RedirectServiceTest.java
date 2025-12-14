package request;

import models.UrlData;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.RedisStore;

import static org.junit.jupiter.api.Assertions.*;

class RedirectServiceTest {
	private RedisStore storage;
	private RedirectService service;

	@BeforeEach
	void setUp() {
		storage = new RedisStore();
		service = new RedirectService(storage);
		storage.listUrls().forEach(u -> storage.deleteUrl(u.getId().toString()));
	}

	@Test
	void throwsWhenUrlNotFound() {
		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.redirect("nonexistent"));
		assertTrue(ex.getMessage().contains("URL не найден"));
	}

	@Test
	void respectsLimitZero() {
		User user = new User();
		UrlData url = new UrlData("https://test.com", "short.com/limit0", user, 0, -1L);
		storage.saveUrl(url);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.redirect("short.com/limit0"));
		assertTrue(ex.getMessage().contains("лимит превышен"));
	}

	@Test
	void respectsExpiredTtl() {
		User user = new User();
		long past = System.currentTimeMillis() - 1000;
		UrlData url = new UrlData("https://test.com", "short.com/expired", user, -1, past);
		storage.saveUrl(url);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> service.redirect("short.com/expired"));
		assertTrue(ex.getMessage().contains("срок действия истек"));
	}
}
