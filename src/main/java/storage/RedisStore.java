package storage;

import config.AppConfig;
import models.UrlData;
import models.User;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RedisStore implements Saveable {

	@Override
	public boolean saveUrl(UrlData urlData) {
		Map<String, String> map = new HashMap<>();
		map.put("id", urlData.getId().toString());
		map.put("fullUrl", urlData.getFullUrl());
		map.put("shortUrl", urlData.getShortUrl());
		map.put("limit", String.valueOf(urlData.getLimit()));
		map.put("user", urlData.getUser().toString());
		map.put("ttl", urlData.getTtl() == -1L ? "-1" : String.valueOf(urlData.getTtl()));

		try (Jedis jedis = new Jedis(AppConfig.REDIS_HOST, AppConfig.REDIS_PORT)) {
			jedis.del("urlData:" + urlData.getId().toString());
			jedis.hset("urlData:" + urlData.getId().toString(), map);
			jedis.set("shortUrl" + urlData.getShortUrl(), urlData.getId().toString());
			return true;
		}
	}

	@Override
	public UrlData readUrl(String key) {
		try (Jedis jedis = new Jedis(AppConfig.REDIS_HOST, AppConfig.REDIS_PORT)) {
			Map<String, String> map = jedis.hgetAll("urlData:" + key);
			if (map.isEmpty()) {
				return null;
			}
			UUID userId = UUID.fromString(map.get("user"));
			User user = new User(userId);

			UUID urlId = UUID.fromString(map.get("id"));
			return new UrlData(urlId, map.get("fullUrl"), map.get("shortUrl"), user, Integer.parseInt(map.get("limit")),
					map.get("ttl").equals("-1") ? -1L : Long.parseLong(map.get("ttl")));
		}
	}

	@Override
	public ArrayList<UrlData> listUrlsByUser(User user) {
		try (Jedis jedis = new Jedis(AppConfig.REDIS_HOST, AppConfig.REDIS_PORT)) {
			ArrayList<UrlData> urlDataList = new ArrayList<>();
			for (String key : jedis.keys("urlData:*")) {
				Map<String, String> map = jedis.hgetAll(key);
				UUID storedUserId = UUID.fromString(map.get("user"));

				if (storedUserId.equals(user.getUuid())) {
					User reconstructedUser = new User(storedUserId);

					UUID urlId = UUID.fromString(map.get("id"));
					UrlData urlData = new UrlData(urlId, map.get("fullUrl"), map.get("shortUrl"), reconstructedUser,
							Integer.parseInt(map.get("limit")),
							map.get("ttl").equals("-1") ? -1L : Long.parseLong(map.get("ttl")));
					urlDataList.add(urlData);
				}
			}
			return urlDataList;
		}
	}

	@Override
	public ArrayList<UrlData> listUrls() {
		ArrayList<UrlData> urlDataList = new ArrayList<>();
		try (Jedis jedis = new Jedis(AppConfig.REDIS_HOST, AppConfig.REDIS_PORT)) {
			for (String key : jedis.keys("urlData:*")) {
				Map<String, String> map = jedis.hgetAll(key);
				UUID userId = UUID.fromString(map.get("user"));
				User user = new User(userId);

				UUID urlId = UUID.fromString(map.get("id"));
				UrlData urlData = new UrlData(urlId, map.get("fullUrl"), map.get("shortUrl"), user,
						Integer.parseInt(map.get("limit")),
						map.get("ttl").equals("-1") ? -1L : Long.parseLong(map.get("ttl")));
				urlDataList.add(urlData);
			}
		}
		return urlDataList;
	}

	@Override
	public boolean deleteUrl(String key) {
		try (Jedis jedis = new Jedis(AppConfig.REDIS_HOST, AppConfig.REDIS_PORT)) {
			jedis.del("urlData:" + key);
			return true;
		} catch (Exception e) {
			throw new RuntimeException("Удаление урла не удалось для ключа: " + key + "\n" + e);
		}
	}

	@Override
	public boolean updateUrl(UrlData urlData) {
		return saveUrl(urlData);
	}

	@Override
	public UrlData findByShortUrl(String shortUrl) {
		try (Jedis jedis = new Jedis(AppConfig.REDIS_HOST, AppConfig.REDIS_PORT)) {
			String id = jedis.get("shortUrl" + shortUrl);
			if (id == null) {
				return null;
			}
			return readUrl(id);
		}
	}
}
