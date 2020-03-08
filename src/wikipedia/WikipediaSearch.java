package wikipedia;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Log;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class WikipediaSearch extends SearchMethod {
    private String WIKIPEDIA_SEARCH_URL = "https://LANG.wikipedia.org/w/api.php" +
            "?action=query" +
            "&format=json" +
            "&list=search" +
            "&utf8=1" +
            "&srsearch=SEARCH";

    private String NEAR_MATCH = "&srwhat=nearmatch";

    public WikipediaSearch() { }

    public WikipediaSearch(boolean nearMatch) {
        if (nearMatch) {
            WIKIPEDIA_SEARCH_URL += NEAR_MATCH;
        }
    }

    private ArrayList<String> getTitleFromAPIResponse(String response) {
        try {
            ArrayList<String> allTitles = new ArrayList<>();

            JSONObject obj = new JSONObject(response);

            if (obj.has("query") && obj.getJSONObject("query").has("search")) {
                JSONArray pages = obj.getJSONObject("query").getJSONArray("search");

                if (pages.length() > 0) {

                    if (((JSONObject) pages.get(0)).getString("snippet").contains("#REDIRECT")) {
                        String redirectTitle = ((JSONObject) pages.get(0)).getString("snippet");
                        allTitles.add(redirectTitle.substring(redirectTitle.indexOf("[[") + 2, redirectTitle.indexOf("]]")));

                        Log.detail(((JSONObject) pages.get(0)).getString("title") + "->" + redirectTitle.substring(redirectTitle.indexOf("[[") + 2, redirectTitle.indexOf("]]")));
                    } else {
                        allTitles.add(((JSONObject) pages.get(0)).getString("title"));
                    }
                }
            }

            return allTitles;
        } catch (Exception e) {
            Log.error("Unexpected response format in " + this.getClass().getSimpleName());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ArrayList<String> search(String expression) {
        ArrayList<String> titles = getTitleFromAPIResponse(WikipediaAPIInteractor.search(expression, WIKIPEDIA_SEARCH_URL.replace("LANG", language)));

        if (titles != null) {
            titles = titles.stream()
                    .map(StringEscapeUtils::unescapeJava)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return titles;
    }
}