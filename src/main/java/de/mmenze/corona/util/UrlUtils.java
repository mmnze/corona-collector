package de.mmenze.corona.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlUtils {

    public static String getContentType(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.setInstanceFollowRedirects(true);
        return connection.getContentType();
    }

}
