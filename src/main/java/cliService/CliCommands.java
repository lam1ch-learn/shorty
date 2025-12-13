package cliService;

import models.UrlData;
import models.User;
import request.RedirectService;
import storage.Saveable;
import cliService.UrlValidator;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class CliCommands {
	private final Saveable storage;
	private final RedirectService redirectService;
	private final User user;
	private final Scanner scanner;
	private final UrlConverter urlConverter;

	public CliCommands(Saveable storage, RedirectService redirectService, User user) {
		this.storage = storage;
		this.redirectService = redirectService;
		this.user = user;
		this.scanner = new Scanner(System.in);
		this.urlConverter = new UrlConverter(storage);
	}

	public void run() {
		while (true) {
			printMenu();
			String command = scanner.nextLine();

			if (command.isEmpty()) {
				System.out.println("Пустая команда. Попробуйте снова.");
				continue;
			}

			switch (command) {
				case "1" -> handleCreateShortUrl();
				case "2" -> handleRedirectShortUrl();
				case "3" -> handleListUserUrls();
				case "4" -> handleEditShortUrl();
				case "5" -> handleDeleteShortUrl();
				case "6" -> {
					System.out.println("Выход из программы.");
					scanner.close();
					return;
				}
				default -> System.out.println("Неизвестная команда. Попробуйте снова.");
			}
		}
	}

	private void printMenu() {
		System.out.println("""
                Введите команду:

                1. Создать короткую ссылку
                2. Перейти по короткой ссылке
                3. Мои ссылки
                4. Редактировать ссылку
                5. Удалить ссылку
                6. Выход""");
	}

	private void handleCreateShortUrl() {
		System.out.println("Введите полный URL (например, https://example.com):");
		String fullUrl = scanner.nextLine().trim();

		if (fullUrl.isEmpty()) {
			System.out.println("URL не может быть пустым.");
			return;
		}

		try {
			String normalizedUrl = UrlValidator.normalizeUrl(fullUrl);
			System.out.println("URL проверен: " + normalizedUrl);
			fullUrl = normalizedUrl;
		} catch (IllegalArgumentException e) {
			System.out.println("Передан неверный URL " + e.getMessage());
			System.out.println("\nПример корректного URL: https://google.com");
			return;
		}

		System.out.println("Введите лимит переходов (-1 для неограниченного):");
		int limit = -1;
		try {
			String limitInput = scanner.nextLine().trim();
			if (!limitInput.isEmpty()) {
				limit = Integer.parseInt(limitInput);
				if (limit < -1) {
					System.out.println("Лимит не может быть меньше -1. Используется -1.");
					limit = -1;
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("Неверный формат лимита. Будет использовано значение по умолчанию (-1).");
		}

		System.out.println("Введите TTL в днях (оставьте пустым для бессрочной ссылки):");
		String ttlInput = scanner.nextLine().trim();
		long expirationTime = -1L;
		if (!ttlInput.isEmpty()) {
			try {
				int days = Integer.parseInt(ttlInput);
				if (days > 0) {
					expirationTime = System.currentTimeMillis() + (long) days * 24 * 60 * 60 * 1000;
				} else {
					System.out.println("Количество дней должно быть положительным. Ссылка будет создана бессрочной.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Неверный формат TTL. Ссылка будет создана бессрочной.");
			}
		}

		UrlData newUrlData = new UrlData(fullUrl, "", user, limit, expirationTime);
		String shortUrl = urlConverter.generateShortUrl(newUrlData);
		System.out.println("🎉 Короткая ссылка создана: " + shortUrl);
	}

	private void handleRedirectShortUrl() {
		System.out.println("Введите короткий URL для перехода:");
		String shortUrl = scanner.nextLine();
		if (shortUrl.isEmpty()) {
			System.out.println("Короткий URL не может быть пустым.");
			return;
		}

		try {
			redirectService.redirect(shortUrl);
			System.out.println("Переход выполнен успешно.");
		} catch (RuntimeException e) {
			System.out.println("Ошибка при переходе: " + e.getMessage());
		}
	}

	private void handleListUserUrls() {
		System.out.println("Ваши ссылки:");
		storage.listUrlsByUser(user).forEach(urlData -> {
			System.out.println("Full URL: " + urlData.getFullUrl());
			System.out.println("Short URL: " + urlData.getShortUrl());

			String limitStatus;
			if (urlData.getLimit() == -1) {
				limitStatus = "Неограничен";
			} else if (urlData.getLimit() <= 0) {
				limitStatus = "Лимит исчерпан";
			} else {
				limitStatus = "Осталось переходов: " + urlData.getLimit();
			}
			System.out.println("Лимит: " + limitStatus);

			String ttlStatus;
			if (urlData.getTtl() == -1L) {
				ttlStatus = "Бессрочная";
			} else {
				long currentTime = System.currentTimeMillis();
				if (currentTime > urlData.getTtl()) {
					ttlStatus = "Просрочена";
				} else {
					long remainingHours = TimeUnit.MILLISECONDS.toHours(urlData.getTtl() - currentTime);
					ttlStatus = "Действительна (осталось " + remainingHours + " часов)";
				}
			}
			System.out.println("TTL: " + ttlStatus);
			System.out.println("---------------------------");
		});
	}

	private void handleEditShortUrl() {
		System.out.println("Введите короткий URL для редактирования:");
		String shortUrlToEdit = scanner.nextLine();
		if (shortUrlToEdit.isEmpty()) {
			System.out.println("Короткий URL не может быть пустым.");
			return;
		}

		UrlData urlData = storage.findByShortUrl(shortUrlToEdit);
		if (urlData == null) {
			System.out.println("Ссылка с коротким URL '" + shortUrlToEdit + "' не найдена.");
			return;
		}

		// Проверка прав владения
		if (!urlData.getUser().getUuid().equals(user.getUuid())) {
			System.out.println("У вас нет прав для редактирования этой ссылки.");
			return;
		}

		System.out.println("Текущий лимит: " + (urlData.getLimit() == -1 ? "Неограничен" : urlData.getLimit()));
		System.out.println("Введите новый лимит переходов (-1 для неограниченного, оставьте пустым для сохранения текущего):");
		String newLimitInput = scanner.nextLine();
		if (!newLimitInput.isEmpty()) {
			try {
				int newLimit = Integer.parseInt(newLimitInput);
				urlData.setLimit(newLimit);
			} catch (NumberFormatException e) {
				System.out.println("Неверный формат лимита. Лимит не изменен.");
			}
		}

		System.out.println("Текущий TTL: " + (urlData.getTtl() == -1L ? "Бессрочная" : TimeUnit.MILLISECONDS.toDays(urlData.getTtl() - System.currentTimeMillis()) + " дн."));
		System.out.println("Введите новый TTL в днях (оставьте пустым для бессрочной, 0 для сохранения текущего):");
		String newTtlInput = scanner.nextLine();
		if (!newTtlInput.isEmpty()) {
			try {
				int days = Integer.parseInt(newTtlInput);
				if (days > 0) {
					urlData.setTtl(System.currentTimeMillis() + (long) days * 24 * 60 * 60 * 1000);
				} else if (days == -1) {
					urlData.setTtl(-1L);
				} else if (days == 0) {
				} else {
					System.out.println("Количество дней должно быть положительным или -1 для бессрочной. TTL не изменен.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Неверный формат TTL. TTL не изменен.");
			}
		}

		try {
			storage.updateUrl(urlData);
			System.out.println("Ссылка '" + shortUrlToEdit + "' успешно обновлена.");
		} catch (Exception e) {
			System.out.println("Ошибка при обновлении ссылки: " + e.getMessage());
		}
	}

	private void handleDeleteShortUrl() {
		System.out.println("Введите короткий URL для удаления:");
		String shortUrlToDelete = scanner.nextLine();
		if (shortUrlToDelete.isEmpty()) {
			System.out.println("Короткий URL не может быть пустым.");
			return;
		}

		UrlData urlData = storage.findByShortUrl(shortUrlToDelete);
		if (urlData == null) {
			System.out.println("Ссылка с коротким URL '" + shortUrlToDelete + "' не найдена.");
			return;
		}

		if (!urlData.getUser().getUuid().equals(user.getUuid())) {
			System.out.println("У вас нет прав для удаления этой ссылки.");
			return;
		}

		System.out.println("Вы уверены, что хотите удалить ссылку '" + shortUrlToDelete + "'? (да/нет)");
		String confirmation = scanner.nextLine();
		if (confirmation.equalsIgnoreCase("да")) {
			try {
				storage.deleteUrl(urlData.getId().toString());
				System.out.println("Ссылка '" + shortUrlToDelete + "' успешно удалена.");
			} catch (Exception e) {
				System.out.println("Ошибка при удалении ссылки: " + e.getMessage());
			}
		} else {
			System.out.println("Удаление отменено.");
		}
	}
}
