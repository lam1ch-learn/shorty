package storage;

import models.UrlData;
import models.User;
import org.junit.jupiter.api.*;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RedisStoreTest {

	private static RedisStore storage;
	private static final String REDIS_HOST = "localhost";
	private static final int REDIS_PORT = 6379;

	@BeforeAll
	static void beforeAll() {
		storage = new RedisStore();
	}

	@BeforeEach
	void setUp() {
		try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
			for (String key : jedis.keys("urlData:*")) {
				jedis.del(key);
			}
			for (String key : jedis.keys("shortUrl*")) {
				jedis.del(key);
			}
		}
	}

	@Test
	void saveAndReadUrl() {
		User user = new User();
		UrlData urlData = new UrlData("https://example.com", "short.com/abc123", user, -1, -1L);

		assertTrue(storage.saveUrl(urlData));
		UrlData loaded = storage.findByShortUrl("short.com/abc123");
		assertNotNull(loaded);
		assertEquals(urlData.getFullUrl(), loaded.getFullUrl());
		assertEquals(urlData.getShortUrl(), loaded.getShortUrl());
		assertEquals(user.getUuid(), loaded.getUser().getUuid());
	}

	@Test
	void listUrlsByUserOnlyReturnsUsersUrls() {
		User user1 = new User();
		User user2 = new User();

		UrlData u1 = new UrlData("https://a.com", "short.com/a1", user1, -1, -1L);
		UrlData u2 = new UrlData("https://b.com", "short.com/b1", user2, -1, -1L);

		storage.saveUrl(u1);
		storage.saveUrl(u2);

		var list1 = storage.listUrlsByUser(user1);
		assertEquals(1, list1.size());
		assertEquals("https://a.com", list1.get(0).getFullUrl());

		var list2 = storage.listUrlsByUser(user2);
		assertEquals(1, list2.size());
		assertEquals("https://b.com", list2.get(0).getFullUrl());
	}

	@Test
	void updateUrlPersistsChanges() {
		User user = new User();
		UrlData urlData = new UrlData("https://example.com", "short.com/upd", user, 10, -1L);
		storage.saveUrl(urlData);

		UrlData loaded = storage.findByShortUrl("short.com/upd");
		assertNotNull(loaded);
		assertEquals(10, loaded.getLimit());

		loaded.setLimit(5);
		storage.updateUrl(loaded);

		UrlData reloaded = storage.findByShortUrl("short.com/upd");
		assertEquals(5, reloaded.getLimit());
	}

	@Test
	void deleteUrlRemovesEntry() {
		User user = new User();
		UrlData urlData = new UrlData("https://delete.com", "short.com/del", user, -1, -1L);
		storage.saveUrl(urlData);

		UrlData loaded = storage.findByShortUrl("short.com/del");
		assertNotNull(loaded);

		storage.deleteUrl(loaded.getId().toString());

		UrlData after = storage.findByShortUrl("short.com/del");
		assertNull(after);
	}

	@Test
	void listUrlsReturnsAll() {
		User user1 = new User();
		User user2 = new User();
		storage.saveUrl(new UrlData("https://1.com", "short.com/1", user1, -1, -1L));
		storage.saveUrl(new UrlData("https://2.com", "short.com/2", user2, -1, -1L));

		var all = storage.listUrls();
		assertTrue(all.size() >= 2);
		boolean found1 = all.stream().anyMatch(u -> "short.com/1".equals(u.getShortUrl()));
		boolean found2 = all.stream().anyMatch(u -> "short.com/2".equals(u.getShortUrl()));
		assertTrue(found1 && found2);
	}
}
