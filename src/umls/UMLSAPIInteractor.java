package umls;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import utils.Log;

import java.io.IOException;

public class UMLSAPIInteractor {
    private final static String API_KEY = "17ce5e74-9b04-4803-8be4-53d3f28366c3";
    private final static String SERVICE = "http://umlsks.nlm.nih.gov";
    private final static String AUTH_URI = "https://utslogin.nlm.nih.gov/cas/v1/api-key";
    private final static String TICKET_URI = "https://utslogin.nlm.nih.gov/cas/v1/tickets/";
    private static UMLSAPIInteractor apiInst = null;

    private String tgt;

    private UMLSAPIInteractor() {
        tgt = getTgt();
        apiInst = this;
    }

    static UMLSAPIInteractor getAPIInstance() {
        if (apiInst == null) {
            new UMLSAPIInteractor();
        }

        return apiInst;
    }

    private String getTgt() {
        try {
            Connection.Response response = Jsoup
                    .connect(AUTH_URI).requestBody("apikey=" + API_KEY)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .method(Connection.Method.POST)
                    .followRedirects(false)
                    .execute();

            return response.header("Location").substring(response.header("Location").indexOf("TGT"));
        } catch (IOException e) {
            Log.error("Unable to obtain Ticket Generator Ticket.");
            e.printStackTrace();
            return null;
        }
    }

    private String getSt() {
        try {
            Connection.Response response = Jsoup
                    .connect(TICKET_URI + tgt).requestBody("service=" + SERVICE)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .method(Connection.Method.POST)
                    .followRedirects(false)
                    .execute();

            return response.body();
        } catch (IOException e) {
            Log.error("Unable to obtain Service Ticket.");
            e.printStackTrace();
            return null;
        }
    }

    JSONObject getFromAPI(String requestUrl) {
        return  getFromAPI(requestUrl, "", false);
    }

    // parameters must include «&»
    JSONObject getFromAPI(String requestUrl, String parameters, boolean suppressErrors) {
        try {
            Connection.Response response = Jsoup
                    .connect("https://uts-ws.nlm.nih.gov" + "/" + requestUrl + "?ticket=" + getSt() + parameters)
                    .ignoreContentType(true)
                    .followRedirects(false)
                    .execute();

            return new JSONObject(response.body());
        } catch (IOException e) {
            if (!suppressErrors) {
                Log.error("Request " + SERVICE + "/" + requestUrl + " failed.");
                e.printStackTrace();
            }
            return null;
        }
    }
}
