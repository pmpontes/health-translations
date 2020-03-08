package wikipedia;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.Log;

import java.util.ArrayList;

public class WikidataSearch extends SearchMethod {
    private static final String WIKIDATA_SEARCH_URL = "https://wikidata.org/w/api.php?action=wbsearchentities&format=json&search=SEARCH&language=LANG";

    private ArrayList<String> getEntityIdFromAPIResponse(String response) {
        try {
            ArrayList<String> entities = new ArrayList<>();
            JSONObject obj = new JSONObject(response);

            if (obj.has("search")) {
                JSONArray results = obj.getJSONArray("search");

                for (Object resultObj: results) {
                    JSONObject result = (JSONObject) resultObj;
                    if (result.has("id")) {
                        entities.add(result.getString("id"));
                    }
                }
            }

            return entities;
        } catch (Exception e) {
            Log.error("Unexpected response format in " + this.getClass().getSimpleName());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ArrayList<String> search(String expression) {
        return getEntityIdFromAPIResponse(WikipediaAPIInteractor.search(expression, WIKIDATA_SEARCH_URL.replace("LANG", language)));
    }
}