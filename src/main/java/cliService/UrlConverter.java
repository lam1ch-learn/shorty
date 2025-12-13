package cliService;

import models.UrlData;
import models.User;
import storage.Saveable;

import java.util.UUID;

public class UrlConverter {
    private final Saveable storage;

    public UrlConverter(Saveable storage) {
        this.storage = storage;
    }

    public String generateShortUrl(UrlData urlData) {
        if (urlData.getShortUrl().isEmpty()) {
            String generatedShortUrl = generateUniqueShortUrl();
            urlData.setShortUrl(generatedShortUrl);
        }

        try {
            storage.saveUrl(urlData);
            return urlData.getShortUrl();
        } catch (Exception e) {
            throw new RuntimeException("Генерация короткого урла не удалась для: " + urlData.getUser().getUuid()
                    + "\n с урлом: " + urlData.getFullUrl() + e);
        }
    }

    private String generateUniqueShortUrl() {
        String generatedShortUrl;
        do {
            generatedShortUrl = "short.com/" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        } while (storage.findByShortUrl(generatedShortUrl) != null);
        return generatedShortUrl;
    }
}
