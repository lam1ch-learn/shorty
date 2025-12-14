package cliService;

import models.UrlData;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.RedisStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TtlCleanUpTest {
	private RedisStore storage;
	private TtlCleanup cleanup;

	@BeforeEach
	void setUp() {
		storage = new RedisStore();
		cleanup = new TtlCleanup(storage, 1);
		storage.listUrls().forEach(u -> storage.deleteUrl(u.getId().toString()));
	}

	@Test
	void detectsExpiredUrls() {
		User user = new User();
		long past = System.currentTimeMillis() - 1000;
		UrlData expired = new UrlData("https://expired.com", "short.com/exp", user, -1, past);
		storage.saveUrl(expired);

		List<UrlData> allUrls = storage.listUrls();
		assertEquals(1, allUrls.size());

		cleanup.cleanupExpiredUrls();

		assertTrue(storage.listUrls().isEmpty());
	}
}
