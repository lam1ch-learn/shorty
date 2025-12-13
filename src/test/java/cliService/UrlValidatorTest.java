package cliService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

	@Test
	void validatesValidUrls() {
		assertTrue(UrlValidator.isValidUrl("https://example.com"));
		assertTrue(UrlValidator.isValidUrl("http://google.com"));
		assertTrue(UrlValidator.isValidUrl("https://yandex.ru/path?query=1"));
	}

	@Test
	void rejectsInvalidUrls() {
		assertFalse(UrlValidator.isValidUrl("google.com"));
		assertFalse(UrlValidator.isValidUrl("ftp://test.com"));
		assertFalse(UrlValidator.isValidUrl("invalid"));
		assertFalse(UrlValidator.isValidUrl(""));
	}

	@ParameterizedTest
	@CsvSource({
			"google.com, https://google.com",
			"http://example.com, http://example.com",
			"https://yandex.ru, https://yandex.ru"
	})
	void normalizesUrls(String input, String expected) {
		String result = UrlValidator.normalizeUrl(input);
		assertEquals(expected, result);
	}
}

