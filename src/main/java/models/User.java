package models;

import java.util.UUID;

public class User {
	private final UUID uuid;

	public User() {
		this.uuid = UUID.randomUUID();
	}

	public User(UUID id) {
		this.uuid = id;
	}

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public String toString() {
		return uuid.toString();
	}
}
