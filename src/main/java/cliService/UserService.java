package cliService;

import models.User;

import java.io.*;
import java.util.UUID;

public class UserService {
	private static final String USER_ID_FILE = "user.id";
	private User currentUser;

	public UserService() {
		loadUser();
	}

	private void loadUser() {
		try (BufferedReader reader = new BufferedReader(new FileReader(USER_ID_FILE))) {
			String uuidString = reader.readLine();
			if (uuidString != null && !uuidString.trim().isEmpty()) {
				currentUser = new User(UUID.fromString(uuidString.trim()));
				System.out.println("Загружен существующий пользователь с UUID: " + currentUser.getUuid());
			} else {
				createNewUser();
			}
		} catch (FileNotFoundException e) {
			System.out.println("Файл пользователя не найден. Создание нового пользователя.");
			createNewUser();
		} catch (IOException | IllegalArgumentException e) {
			System.err.println("Ошибка при загрузке пользователя: " + e.getMessage());
			createNewUser();
		}
	}

	private void createNewUser() {
		currentUser = new User();
		saveUser();
		System.out.println("Создан новый пользователь с UUID: " + currentUser.getUuid());
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void saveUser() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_ID_FILE))) {
			writer.write(currentUser.getUuid().toString());
			System.out.println("UUID пользователя сохранен в файл: " + USER_ID_FILE);
		} catch (IOException e) {
			System.err.println("Ошибка при сохранении пользователя: " + e.getMessage());
		}
	}
}
