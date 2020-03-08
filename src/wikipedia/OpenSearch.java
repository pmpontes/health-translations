package wikipedia;

import utils.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class OpenSearch extends SearchMethod {
    private static final String WIKIPEDIA_OPENSEARCH_URL = "https://LANG.wikipedia.org/w/api.php" +
            "?action=opensearch" +
            "&search=SEARCH" + // search query
            "&limit=10" + // return only the first result
            "&namespace=0" + // search only articles
            "&format=json" +
            "&redirects=resolve";

    private ArrayList<String> getTitleFromAPIResponse(String response) {
        try {
            if (response != null) {
                ArrayList<String> allTitles = new ArrayList<>(Arrays.asList(response.substring(response.indexOf("[", 1) + 1, response.indexOf("]")).replace("\"", "").split(",")));
                Log.detail(allTitles.toString());
                return allTitles;
            }
        } catch (Exception e) {
            Log.error("Unexpected response format in " + this.getClass().getSimpleName());
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public ArrayList<String> search(String expression) {
        return getTitleFromAPIResponse(WikipediaAPIInteractor.search(expression, WIKIPEDIA_OPENSEARCH_URL.replace("LANG", language)));
    }
}