package models;

import java.util.UUID;

public class UrlData {
	private UUID id;
	private String fullUrl = "";
	private String shortUrl = "";
	private int limit = -1;
	private User user;
	private long ttl = -1L;

	public UrlData(String fullUrl, String shortUrl, User user, int limit, long ttl) {
		this.id = UUID.randomUUID();
		this.fullUrl = fullUrl;
		this.shortUrl = shortUrl;
		this.limit = limit;
		this.user = user;
		this.ttl = ttl;
	}

	public UrlData(UUID id, String fullUrl, String shortUrl, User user, int limit, long ttl) {
		this.id = id;
		this.fullUrl = fullUrl;
		this.shortUrl = shortUrl;
		this.limit = limit;
		this.user = user;
		this.ttl = ttl;
	}

	public UUID getId() {
		return id;
	}

	public String getShortUrl() {
		return shortUrl;
	}

	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}

	public String getFullUrl() {
		return fullUrl;
	}

	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public long getTtl() {
		return ttl;
	}

	public void setTtl(long ttl) {
		this.ttl = ttl;
	}
}
