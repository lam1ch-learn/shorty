package cliService;

import models.UrlData;
import storage.Saveable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TtlCleanup {
	private final Saveable storage;
	private final ScheduledExecutorService scheduler;
	private final long cleanupIntervalMinutes;

	public TtlCleanup(Saveable storage, long cleanupIntervalMinutes) {
		this.storage = storage;
		this.cleanupIntervalMinutes = cleanupIntervalMinutes;
		this.scheduler = Executors.newSingleThreadScheduledExecutor();
	}

	public void start() {
		scheduler.scheduleAtFixedRate(this::cleanupExpiredUrls, 0, cleanupIntervalMinutes, TimeUnit.MINUTES);
		System.out.println("TtlCleanupService запущен, проверка каждые " + cleanupIntervalMinutes + " минут.");
	}

	void cleanupExpiredUrls() {
		System.out.println("Запуск проверки просроченных URL и URL с исчерпанным лимитом...");
		List<UrlData> allUrls = storage.listUrls();
		long currentTime = System.currentTimeMillis();
		int deletedCount = 0;

		for (UrlData urlData : allUrls) {
			boolean shouldDelete = false;
			String notificationMessage = "";

			if (urlData.getTtl() != -1L && currentTime > urlData.getTtl()) {
				shouldDelete = true;
				notificationMessage = "\n[УВЕДОМЛЕНИЕ]: Ваша короткая ссылка '" + urlData.getShortUrl()
						+ "' (полный URL: " + urlData.getFullUrl() + ") просрочена и будет удалена.";
			} else if (urlData.getLimit() != -1 && urlData.getLimit() <= 0) {
				shouldDelete = true;
				notificationMessage = "\n[УВЕДОМЛЕНИЕ]: Ваша короткая ссылка '" + urlData.getShortUrl()
						+ "' (полный URL: " + urlData.getFullUrl() + ") исчерпала лимит переходов и будет удалена.";
			}

			if (shouldDelete) {
				System.out.println(notificationMessage);
				try {
					storage.deleteUrl(urlData.getId().toString());
					deletedCount++;
				} catch (Exception e) {
					System.err.println("Ошибка при удалении ссылки " + urlData.getShortUrl() + ": " + e.getMessage());
				}
			}
		}
		System.out.println("Проверка завершена. Удалено " + deletedCount + " ссылок.");
	}

	public void stop() {
		scheduler.shutdown();
		try {
			if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
				scheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			scheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
		System.out.println("TtlCleanupService остановлен.");
	}
}
