package wikipedia;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import translation.Translator;
import utils.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class WikidataTranslation extends TranslationMethod {
    private final String WIKIDATA_ENTITY_URL = "https://wikidata.org/w/api.php?action=wbgetentities&format=json&ids=SEARCH&languages=LANG";
    public static final String DISAMBIGUATION_PAGE = "página de desambiguação";
    private final String PT_PT_BR = "pt%7Cpt-br";
    private final String PT_BR = "pt-br";

    public WikidataTranslation() {
        this.searchMethods = new ArrayList<>();
        this.searchMethods.add(new WikidataSearch());
    }

    private ArrayList<String> getEntityAliasesFromAPIResponse(String id, String response) {
        try {
            ArrayList<String> aliases = new ArrayList<>();
            JSONObject obj = new JSONObject(response);

            if (obj.has("entities")) {
                JSONObject results = obj.getJSONObject("entities");

                if (results.has(id)) {
                    JSONObject entity = results.getJSONObject(id);

                    if (entity.has("labels")) {
                        JSONObject labels = entity.getJSONObject("labels");
                        if (labels.has(languageTo)) {
                            aliases.add(labels.getJSONObject(languageTo).getString("value").toLowerCase());
                        }

                        if (languageTo.equals(Translator.PT)) {
                            if (labels.has(PT_BR)) {
                                aliases.add(labels.getJSONObject(PT_BR).getString("value").toLowerCase());
                            }
                        }
                    }

                    if (entity.has("aliases")) {
                        JSONObject aliasesList = entity.getJSONObject("aliases");

                        if (aliasesList.has(languageTo) && aliasesList.has(languageTo)) {
                            JSONArray languageToAliases = aliasesList.getJSONArray(languageTo);
                            for (Object languageToAlias: languageToAliases) {
                                aliases.add(((JSONObject) languageToAlias).getString("value").toLowerCase());
                            }
                        }

                        if (languageTo.equals(Translator.PT) && aliasesList.has(PT_BR)) {
                            JSONArray languageToAliases = aliasesList.getJSONArray(PT_BR);
                            for (Object languageToAlias: languageToAliases) {
                                aliases.add(((JSONObject) languageToAlias).getString("value").toLowerCase());
                            }
                        }
                    }

                    if (entity.has("descriptions")) {
                        JSONObject descriptions = entity.getJSONObject("descriptions");
                        if (descriptions.has(languageTo)) {
                            if (descriptions.getJSONObject(languageTo).getString("value").toLowerCase().contains(DISAMBIGUATION_PAGE.toLowerCase())) {
                                aliases = aliases.stream().map(alias -> alias.concat(DISAMBIGUATION_PAGE)).collect(Collectors.toCollection(ArrayList::new));
                            }
                        }
                    }
                }
            }

            return aliases;
        } catch (Exception e) {
            Log.error("Unexpected response format in " + this.getClass().getSimpleName());
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<String> translate(String entity) {
        String useLanguage = languageTo.equals(Translator.PT) ? PT_PT_BR : languageTo;
        ArrayList<String> titles = getEntityAliasesFromAPIResponse(entity,
                WikipediaAPIInteractor.search(entity, WIKIDATA_ENTITY_URL.replace("LANG", useLanguage)));

        if (titles != null) {
            Log.detail(titles.toString());
            return titles.stream()
                    .map(StringEscapeUtils::unescapeJava)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return null;
    }
}