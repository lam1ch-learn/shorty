package cliService;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlValidator {

	public static boolean isValidUrl(String urlString) {
		if (urlString == null || urlString.trim().isEmpty()) {
			return false;
		}

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

	public static String normalizeUrl(String originalUrl) {
		if (originalUrl == null || originalUrl.trim().isEmpty()) {
			throw new IllegalArgumentException("URL не может быть пустым");
		}

		if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
			return "https://" + originalUrl;
		}

		return originalUrl;
	}
}
