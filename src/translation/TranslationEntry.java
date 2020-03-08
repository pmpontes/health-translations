package translation;

import utils.Log;
import wikipedia.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TranslationEntry implements Serializable {

    static final long serialVersionUID = 5378302652702785172L;
    protected String originalExpression;
    protected ArrayList<Translation> translationsGenerated;
    protected ArrayList<String> correctTranslations;
    protected TranslationMetrics translationMetrics;

    public TranslationEntry(String originalExpression) {
        this.originalExpression = originalExpression;
        this.translationsGenerated = new ArrayList<>();
        this.correctTranslations = new ArrayList<>();
    }

    public TranslationEntry(String originalExpression, String correctTranslation) {
        this(originalExpression);
        this.correctTranslations.add(correctTranslation);
    }

    public void addCorrectTranslation(String correctTranslation) {
        if (!correctTranslations.contains(correctTranslation)) {
            correctTranslations.add(correctTranslation);
        }
    }

    public String getOriginalExpression() {
        return originalExpression;
    }

    public void setOriginalExpression(String originalExpression) {
        this.originalExpression = originalExpression;
    }

    public ArrayList<Translation> getTranslationsGenerated() {
        return translationsGenerated;
    }

    public ArrayList<String> getCorrectTranslations() {
        return correctTranslations;
    }

    public boolean isTranslationCorrect() {
        for (String correctTranslation: correctTranslations) {
            correctTranslation = Translation.getComparableString(correctTranslation);

            for (Translation translation: translationsGenerated) {
                  if (translation.isTranslationCorrect(correctTranslation)) {
                      return true;
                  }
            }
        }

        Log.detail(this.toString());

        return false;
    }

    public boolean isTranslationClose() {
        for (String correctTranslation: correctTranslations) {
            correctTranslation = Translation.getComparableString(correctTranslation);

            for (Translation translation: translationsGenerated) {
                if (translation.isTranslationClose(correctTranslation)) {
                    return true;
                }
            }
        }

        Log.detail(this.toString());

        return false;
    }

    public boolean isTranslationRelated() {
        for (String correctTranslation: correctTranslations) {
            correctTranslation = Translation.getComparableString(correctTranslation);

            for (Translation translation: translationsGenerated) {
                if (translation.isTranslationRelated(correctTranslation)) {
                    return true;
                }
            }
        }

        Log.detail(this.toString());

        return false;
    }

    public void evaluate() {
        for (String correctTranslation: correctTranslations) {
            correctTranslation = Translation.getComparableString(correctTranslation);

            for (Translation translation: translationsGenerated) {
                translation.evaluate(correctTranslation);
            }
        }
    }



    @Override
    public String toString() {
        return "TranslationEntry{" +
                "originalExpression='" + originalExpression + '\'' +
                ", translationsGenerated='" + translationsGenerated.toString() + '\'' +
                ", correctTranslations='" + correctTranslations.toString() + '\'' +
                "}\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslationEntry that = (TranslationEntry) o;

        return originalExpression.equals(that.originalExpression);
    }

    @Override
    public int hashCode() {
        return originalExpression.hashCode();
    }

    public void addTranslationGenerated(Translation possibleTranslation) {
        if (possibleTranslation != null &&!translationsGenerated.contains(possibleTranslation)) {
            translationsGenerated.add(possibleTranslation);
        }
    }

    public TranslationMetrics getTranslationMetrics() {
        if (translationMetrics == null) {
            translationMetrics = new TranslationMetrics(translationsGenerated, correctTranslations);
        }

        return translationMetrics;
    }

    public void keepNTranslations(int nTranlations) {
        if (translationsGenerated.size() > nTranlations) {
            translationsGenerated = translationsGenerated.stream().limit(nTranlations).collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public void generateTranslations(List<TranslationMethod> translationMethods) {
        for (TranslationMethod translationMethod: translationMethods) {
            ArrayList<Translation> possibleTranslations = translationMethod.translateExpression(originalExpression);
            if (possibleTranslations != null && !possibleTranslations.isEmpty()) {
                Log.dump(possibleTranslations);
                possibleTranslations.forEach(this::addTranslationGenerated);
            }
        }
    }

    public boolean hasWikidataConcept() {
        for (Translation translation : translationsGenerated) {
            if (translation.getTranslationMethod() instanceof WikidataTranslation) {
                return true;
            }
        }

        return false;
    }

    public boolean hasWikipediaArticle() {
        for (Translation translation : translationsGenerated) {
            if (translation.getTranslationMethod() instanceof LangLinksTranslation) {
                return true;
            }
        }

        return false;
    }
}
