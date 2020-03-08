package wikipedia;

import org.apache.commons.lang.StringEscapeUtils;
import utils.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class WikipediaAPIInteractor {
    private static String doGet(String requestUrl) {
        try {
            URL obj = new URL(requestUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            Log.detail("Sending 'GET' request to URL : " + requestUrl);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            Log.detail(response.toString());
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String search(String expression, String url) {
        try {
            expression = StringEscapeUtils.unescapeJava(expression);
            String requestUrl = url.replace("SEARCH", URLEncoder.encode(expression.trim(), "UTF-8"));
            return doGet(requestUrl);
        } catch (UnsupportedEncodingException e) {
            Log.error("Unexpected error in WikipediaAPIInteractor");
            e.printStackTrace();
            return null;
        }
    }
}
