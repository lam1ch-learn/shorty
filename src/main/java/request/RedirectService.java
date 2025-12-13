package request;

import models.UrlData;
import storage.Saveable;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RedirectService {

	private final Saveable storage;

	public RedirectService(Saveable storage) {
		this.storage = storage;
	}

	public void redirect(String shortUrl) {
		UrlData urlData = storage.findByShortUrl(shortUrl);
		if (urlData == null) {
			throw new RuntimeException("URL не найден для short URL: " + shortUrl);
		}
		try {
			if (urlData.getLimit() != -1 && urlData.getLimit() <= 0) {
				throw new RuntimeException("URL лимит превышен для short URL: " + shortUrl);
			}
			if (urlData.getTtl() != -1L && System.currentTimeMillis() > urlData.getTtl()) {
				throw new RuntimeException("URL срок действия истек для short URL: " + shortUrl);
			} else {
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI(urlData.getFullUrl()));
					urlData.setLimit(urlData.getLimit() - 1);
					storage.updateUrl(urlData);
				} else {
					throw new RuntimeException("Desktop не поддерживается на этой платформе.");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Ошибка при открытии страницы " + e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Ошибка в синтаксисе FullUrl" + urlData.getFullUrl() + e);
		}
	}
}
