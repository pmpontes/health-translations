package wikipedia;

import org.json.JSONArray;
import org.json.JSONObject;
import translation.Translation;
import utils.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class TranslationMethod implements Serializable{
    protected List<SearchMethod> searchMethods;
    protected String languageFrom;
    protected String languageTo;

    public void setLanguageTo(String language) {
        this.languageTo = language;
    }

    public void setLanguageFrom(String language) {
        this.searchMethods.forEach(searchMethod -> searchMethod.setLanguage(language));
        this.languageFrom = language;
    }

    protected String getTitleFromAPIResponse(String response) {
        try {
            HashMap<String, String> titles = new HashMap<>();
            JSONObject obj = new JSONObject(response);

            if (obj.has("query") && obj.getJSONObject("query").has("pages")) {
                JSONObject pages = obj.getJSONObject("query").getJSONObject("pages");
                pages.keySet().stream().filter(id -> pages.getJSONObject(id).has("langlinks")).forEach(id -> {
                    JSONArray translations = pages.getJSONObject(id).getJSONArray("langlinks");
                    titles.put(pages.getJSONObject(id).getString("title"), ((JSONObject) translations.get(0)).getString("*"));
                });
            }

            if (!titles.isEmpty()) {

                return titles.values().iterator().next();
            } else {

                return null;
            }
        } catch (Exception e) {
            Log.error("Unexpected response format in " + this.getClass().getSimpleName());
            e.printStackTrace();

            return null;
        }
    }

    private ArrayList<Translation> searchAndTranslate(String originalExpression, SearchMethod method) {
        ArrayList<Translation> translations = new ArrayList<>();

        ArrayList<String> searchResults = method.search(originalExpression);
        if (searchResults != null) {
            for (String expression: searchResults) {
                ArrayList<String> possibleTranslations = translate(expression);
                if (possibleTranslations != null) {
                    possibleTranslations.forEach(translation -> {
                        Translation possibleTranslation = new Translation(languageTo, translation, this, method);
                        if (translation.contains(WikidataTranslation.DISAMBIGUATION_PAGE)) {
                            possibleTranslation.setObservations(WikidataTranslation.DISAMBIGUATION_PAGE);
                            possibleTranslation.setTranslationGenerated(translation.replace(WikidataTranslation.DISAMBIGUATION_PAGE, ""));
                        }
                        translations.add(possibleTranslation);
                    });
                }
            }
        }

        if (translations.isEmpty()) {
            Log.detail("LangLinksTranslation for " + originalExpression + " not found using " + method.getClass().getSimpleName() + " and " +
                    getClass().getSimpleName());
        }

        return translations;
    }

    public ArrayList<Translation> translateExpression(String expression) {
        ArrayList<Translation> translations = new ArrayList<>();

        for (SearchMethod searchMethod: searchMethods) {
            translations.addAll(searchAndTranslate(expression, searchMethod));
        }

        return translations;
    }

    protected String clearString(String str) {
        str = str.replace("?", "");
        if (str.contains("(") && str.contains(")")) {
            String subStr = str.substring(str.indexOf("("), str.indexOf(")"));
            return str.replace(subStr + ")", "").trim();
        }

        return str.replace("/", "").trim();
    }

    abstract ArrayList<String> translate(String expression);
}