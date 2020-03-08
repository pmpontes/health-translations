package translation;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.tartarus.snowball.ext.*;
import utils.Log;
import utils.frenchStemmer;
import wikipedia.SearchMethod;
import wikipedia.TranslationMethod;

import java.io.Serializable;

public class Translation implements Serializable {
    private final String language;
    private TranslationMethod translationMethod;
    private SearchMethod searchMethod;
    private String translationGenerated;
    private String observations;
    private float distance = Float.MAX_VALUE;

    public Translation(String language, String translation) {
        this.language = language;
        this.translationGenerated = translation;
    }

    public Translation(Translation translation1, Translation translation2) {
        this(translation1.language, translation1.translationGenerated + " " + translation2.translationGenerated,
                translation1.translationMethod, translation1.searchMethod);
    }

    public Translation(String language, String translation, TranslationMethod translationMethod, SearchMethod searchMethod) {
        this(language, translation);
        this.translationMethod = translationMethod;
        this.searchMethod = searchMethod;
    }

    public void setTranslationGenerated(String translationGenerated) {
        this.translationGenerated = translationGenerated;
    }

    public TranslationMethod getTranslationMethod() {
        return translationMethod;
    }

    public void setTranslationMethod(TranslationMethod translationMethod) {
        this.translationMethod = translationMethod;
    }

    public SearchMethod getSearchMethod() {
        return searchMethod;
    }

    public void setSearchMethod(SearchMethod searchMethod) {
        this.searchMethod = searchMethod;
    }

    public String getTranslationGenerated() {
        return translationGenerated;
    }

    public void evaluate(String correctTranslation) {
        // evaluate quality of translation
        float newDistance = new LevenshteinDistance().apply(getComparableString(translationGenerated), correctTranslation);
        if (newDistance < distance) {
            distance = newDistance;
        }
    }

    public static String getComparableString(String str) {
        if (str != null) {
            return StringUtils.stripAccents(str.toLowerCase().replace(",", "").replace("-", "").trim());
        }

        return null;
    }

    private String getStemmedString(String str) {
        switch (language) {
            case Translator.PT: {
                portugueseStemmer stemmer = new portugueseStemmer();
                stemmer.setCurrent(str);
                if (stemmer.stem()) {
                    return stemmer.getCurrent();
                }
            }
            case Translator.EN: {
                englishStemmer stemmer = new englishStemmer();
                stemmer.setCurrent(str);
                if (stemmer.stem()) {
                    return stemmer.getCurrent();
                }
            }
            case Translator.ES: {
                spanishStemmer stemmer = new spanishStemmer();
                stemmer.setCurrent(str);
                if (stemmer.stem()) {
                    return stemmer.getCurrent();
                }
            }
            case Translator.IT: {
                italianStemmer stemmer = new italianStemmer();
                stemmer.setCurrent(str);
                if (stemmer.stem()) {
                    return stemmer.getCurrent();
                }
            }
            case Translator.DE: {
                germanStemmer stemmer = new germanStemmer();
                stemmer.setCurrent(str);
                if (stemmer.stem()) {
                    return stemmer.getCurrent();
                }
            }
            case Translator.FR: {
                frenchStemmer stemmer = new frenchStemmer();
                stemmer.setCurrent(str);
                if (stemmer.stem()) {
                    return stemmer.getCurrent();
                }
            }
            default:
                return str;
        }
    }

    public boolean isTranslationCorrect(String correctTranslation) {
        return translationGenerated != null && getComparableString(translationGenerated).equals(correctTranslation);
    }

    public boolean isTranslationClose(String correctTranslation) {
        return translationGenerated != null && getStemmedString(getComparableString(translationGenerated)).equals(correctTranslation);
    }

    public boolean isTranslationRelated(String correctTranslation) {
        if (translationGenerated != null) {
            String[] expressions = translationGenerated.split(" ");

            for (String expression : expressions) {
                if (getComparableString(expression).equals(correctTranslation)) {
                    return true;
                }
            }

            expressions = correctTranslation.split(" ");
            String comparableTranslationGenerated = getComparableString(translationGenerated);
            for (String expression : expressions) {
                if (expression.equals(comparableTranslationGenerated)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "distance=" + distance +
                ", translationGenerated='" + translationGenerated + '\'' +
                (translationMethod != null ? ", translationMethod=" + translationMethod.getClass().getSimpleName() : "") +
                (searchMethod != null ? ", searchMethod=" + searchMethod.getClass().getSimpleName() : "") +
                "}\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Translation that = (Translation) o;

        return translationGenerated.equals(that.translationGenerated);
    }

    @Override
    public int hashCode() {
        return translationGenerated.hashCode();
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
