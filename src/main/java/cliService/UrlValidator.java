package cliService;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlValidator {

	public static boolean isValidUrl(String urlString) {
		if (urlString == null || urlString.trim().isEmpty()) {
			return false;
		}

		// Простая проверка на базовый формат
		if (!urlString.contains("://")) {
			return false;
		}

		try {
			new URL(urlString);

			String protocol = urlString.split("://")[0].toLowerCase();
			return protocol.equals("http") || protocol.equals("https");

		} catch (MalformedURLException e) {
			return false;
		}
	}

	public static String normalizeUrl(String urlString) {
		if (!isValidUrl(urlString)) {
			throw new IllegalArgumentException("Некорректный URL: " + urlString);
		}

		if (!urlString.contains("://")) {
			return "https://" + urlString;
		}
		return urlString;
	}
}

