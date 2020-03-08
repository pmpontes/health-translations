package wikipedia;

import translation.Translation;
import translation.TranslationEntry;
import translation.TranslationSource;
import utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WikipediaTranslator extends TranslationSource {

    private TranslationEntry translationEntry;
    private List<SearchMethod> searchMethods = Arrays.asList(new OpenSearch(), new WikipediaSearch());
    private List<TranslationMethod> translationMethods = Arrays.asList((new WikidataTranslation()), (new LangLinksTranslation(searchMethods)));

    public WikipediaTranslator() { }

    public WikipediaTranslator(TranslationEntry translationEntry, String languageFrom, String languageTo) {
        this.translationEntry = translationEntry;

        translationMethods.forEach(translationMethod -> {
            translationMethod.setLanguageTo(languageTo);
            translationMethod.setLanguageFrom(languageFrom);
        });

        this.run();
    }

    private void translateExpression() {
        translationEntry.generateTranslations(translationMethods);
        if (translationEntry.getTranslationsGenerated().isEmpty()) {
            Log.detail("No translation for " + getExpression() + " found.");
        }
    }

    @Override
    public void run() {
        translateExpression();
    }

    public String getExpression() {
        return translationEntry.getOriginalExpression();
    }

    @Override
    public TranslationSource createTranslator(TranslationEntry translationEntry, String languageFrom, String languageTo) {
        return new WikipediaTranslator(translationEntry, languageFrom, languageTo);
    }
}
