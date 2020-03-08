package translation;

public abstract class TranslationSource extends Thread {
    public TranslationSource() {

    }

    public abstract String getExpression();

    public abstract TranslationSource createTranslator(TranslationEntry translationEntry, String languageFrom, String languageTo);
}
