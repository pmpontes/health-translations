package wikipedia;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

import static wikipedia.WikipediaAPIInteractor.search;

public class LangLinksTranslation extends TranslationMethod {
    private static final String WIKIPEDIA_LANG_URL = "https://LANG_FROM.wikipedia.org/w/api.php" +
            "?action=query" +
            "&titles=SEARCH" +
            "&prop=langlinks" +
            "&lllimit=500" +
            "&lllang=LANG_TO" +
            "&format=json";

    public LangLinksTranslation(List<SearchMethod> searchMethods) {
        this.searchMethods = searchMethods;
    }

    ArrayList<String> translate(String expression) {
        ArrayList<String> translations = new ArrayList<>();

        String translation = getTitleFromAPIResponse(
                search(expression, WIKIPEDIA_LANG_URL.replace("LANG_TO", languageTo).replace("LANG_FROM", languageFrom)));

        if (translation != null) {
            translations.add(StringEscapeUtils.unescapeJava(clearString(translation)));
        }

        return translations;
    }
}