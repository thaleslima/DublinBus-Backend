package net.dublin.bus.backend.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class HttpUtil {

    private static InputStream openUrlGetJsonInputStream(String url) throws Exception {
        InputStream body;
        URL url2 = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
        connection.setRequestProperty("Content-Type", "application/json; charset=iso-8859-1");
        connection.setRequestMethod("GET");
        int HttpResult = connection.getResponseCode();
        body = connection.getInputStream();
        if (HttpResult != 200) {
            throw new Exception("Error openUrlGetInputStream");
        }
        return body;
    }

    public static String openUrlGet(String url) throws Exception {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(openUrlGetJsonInputStream(url)), "iso-8859-1"));
        String inputLine;
        StringBuilder response2 = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response2.append(inputLine);
        }
        in.close();

        return response2.toString();
    }
}
