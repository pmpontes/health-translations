package google;

import com.google.api.GoogleAPI;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import translation.Translation;
import translation.TranslationEntry;
import translation.TranslationSource;
import utils.Log;

public class GoogleTranslator extends TranslationSource {
    private static final String GOOGLE_API_KEY  = "AIzaSyAp8IW94lMAGBcSOcSmntUlwLUguj1sASQ";
    private static final String EN = "en";
    private static final String PT = "pt";
    private String languageFrom;
    private String languageTo;
    private TranslationEntry translationEntry;

    public GoogleTranslator() {}

    public GoogleTranslator(TranslationEntry translationEntry, String languageFrom, String languageTo) {
        this.translationEntry = translationEntry;
        this.languageFrom = languageFrom;
        this.languageTo = languageTo;
        this.run();
    }

    private void translateExpression() {
        try {
            GoogleAPI.setKey(GOOGLE_API_KEY);
            GoogleAPI.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");

            String translatedText = Translate.DEFAULT.execute(getExpression(), Language.fromString(languageFrom), Language.fromString(languageTo));

            Translation translation = new Translation(languageTo, translatedText);
            translationEntry.addTranslationGenerated(translation);

            Log.detail(translationEntry.getOriginalExpression() + " translated to " + translation.getTranslationGenerated());
        } catch (Exception e) {
            Log.error("LangLinksTranslation for " + getExpression() + " not found.");
            e.printStackTrace();
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
        return new GoogleTranslator(translationEntry, languageFrom, languageTo);
    }

    // for test purposes only
    public static void main(String[] args) {
        GoogleTranslator t = new GoogleTranslator(new TranslationEntry("heart attack"), EN, PT);

        try {
            t.join();
            Log.info(t.translationEntry.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}