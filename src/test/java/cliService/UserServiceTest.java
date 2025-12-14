package cliService;

import models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
	private static final String USER_FILE = "user.id";

	@AfterEach
	void cleanup() {
		File f = new File(USER_FILE);
		if (f.exists())
			f.delete();
	}

	@Test
	void createsNewUserWhenNoFile() {
		File f = new File(USER_FILE);
		if (f.exists())
			f.delete();

		UserService service = new UserService();
		User user = service.getCurrentUser();
		assertNotNull(user.getUuid());
		assertTrue(new File(USER_FILE).exists());
	}

	@Test
	void loadsExistingUserFromFile() {
		UserService service1 = new UserService();
		User user1 = service1.getCurrentUser();

		UserService service2 = new UserService();
		User user2 = service2.getCurrentUser();
		assertEquals(user1.getUuid(), user2.getUuid());
	}
}
