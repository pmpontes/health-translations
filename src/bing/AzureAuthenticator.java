package bing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import utils.Log;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class AzureAuthenticator {
    private static AzureAuthenticator inst;

    // URL of the token service
    private static final String SERVICE_URL = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
    private static final String API_SUBSCRIPTION_KEY = "17b338df00be4d419a52e29ac49792c6";

    // Name of header used to pass the subscription key to the token service
    private static final String OCP_APIM_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";

    // After obtaining a valid token, this class will cache it for this duration.
    // Use a duration of 9 minutes, which is less than the actual token lifetime of 10 minutes.
    private static final Duration tokenCacheDuration = Duration.ofMinutes(9);

    // Cache the value of the last valid token obtained from the token service.
    private String storedToken = "";

    // When the last valid token was obtained.
    private LocalDateTime storedTokenTime = LocalDateTime.MIN;

    // Gets the subscription key.
    private String subscriptionKey;

    private AzureAuthenticator() {
        this(API_SUBSCRIPTION_KEY);
    }

    private AzureAuthenticator(String key) {
        this.subscriptionKey = key;
    }

    public static AzureAuthenticator getInstance() {
        if (inst == null) {
            inst = new AzureAuthenticator();
        }

        return inst;
    }

    // <summary>
    // Gets a token for the specified subscription.
    // </summary>
    // <returns>The encoded JWT token prefixed with the string "Bearer ".</returns>
    // <remarks>
    // This method uses a cache to limit the number of request to the token service.
    // A fresh token can be re-used during its lifetime of 10 minutes. After a successful
    // request to the token service, this method caches the access token. Subsequent
    // invocations of the method return the cached token for the next 5 minutes. After
    // 5 minutes, a new token is fetched from the token service and the cache is updated.
    // </remarks>
    public String getAccessToken() {
        if (subscriptionKey.isEmpty()) {
            return null;
        }

        // re-use the cached token if there is one.
        if (Duration.between(storedTokenTime, LocalDateTime.now()).compareTo(tokenCacheDuration) < 0) {
            return storedToken;
        }

        try {
            Connection.Response response = Jsoup
                    .connect(SERVICE_URL).requestBody("")
                    .header(OCP_APIM_SUBSCRIPTION_KEY_HEADER, subscriptionKey)
                    .ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .followRedirects(false)
                    .execute();

            storedTokenTime = LocalDateTime.now();
            storedToken = "Bearer " + response.body();
            return storedToken;
        } catch (IOException e) {
            Log.error("Unable to obtain Token.");
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        Log.info(new AzureAuthenticator().getAccessToken());
    }
}