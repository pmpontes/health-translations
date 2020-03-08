package bing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import translation.Translation;
import translation.TranslationEntry;
import translation.TranslationSource;
import utils.Log;

import java.net.URLEncoder;

public class BingTranslator extends TranslationSource {
    private static final String URL_TRANSLATE = "https://api.microsofttranslator.com/v2/Ajax.svc/Translate?text=";
    private static final String URL_TRANSLATE_ARRAY  = "https://api.microsofttranslator.com/v2/Ajax.svc/TranslateArray?text=";
    private static final String EN = "en";
    private static final String PT = "pt";
    private String languageFrom;
    private String languageTo;
    private TranslationEntry translationEntry;

    public BingTranslator() {}

    public BingTranslator(TranslationEntry translationEntry, String languageFrom, String languageTo) {
        this.translationEntry = translationEntry;
        this.languageFrom = languageFrom;
        this.languageTo = languageTo;
        this.run();
    }

    // convert from internal Java String format -> UTF-8
    private static String convertToUTF8(String s) {
        try {
            String out = new String(s.getBytes("ISO-8859-1"), "UTF-8");
            return out.replace("?", "");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
    }

    private void translateExpression() {
        try {
            String requestUrl = URL_TRANSLATE + URLEncoder.encode(getExpression(), "UTF-8") + "&from=" + languageFrom + "&to=" + languageTo + "&format=json";

            Connection.Response response = Jsoup
                    .connect(requestUrl)
                    .ignoreContentType(true)
                    .header("Authorization", AzureAuthenticator.getInstance().getAccessToken())
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .execute();

            Translation translation = new Translation(languageTo, convertToUTF8(response.body().replace("\"", "")));
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
        return new BingTranslator(translationEntry, languageFrom, languageTo);
    }

    // for test purposes only
    public static void main(String[] args) {
        BingTranslator t = new BingTranslator(new TranslationEntry("heart attack"), EN, PT);

        try {
            t.join();
            Log.info(t.translationEntry.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}