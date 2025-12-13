package storage;

import models.UrlData;
import models.User;

import java.util.ArrayList;

public interface Saveable {

    public boolean saveUrl(UrlData urlData);

    public UrlData readUrl(String key);

	public boolean deleteUrl(String key);

	public boolean updateUrl(UrlData urlData);

	public UrlData findByShortUrl(String shortUrl);

    public ArrayList<UrlData> listUrls();

    public  ArrayList<UrlData> listUrlsByUser(User user);
}
