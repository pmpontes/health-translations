package translation;

import utils.CSV;
import utils.Log;
import wikipedia.WikidataTranslation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator implements Serializable {
    public static final String EN = "en";
    public static final String PT = "pt";
    public static final String FR = "fr";
    public static final String IT = "it";
    public static final String ES = "es";
    public static final String DE = "de";

    private List<TranslationEntry> translationEntries;
    private String languageTo;
    private String languageFrom;

    public Translator(String filePath) {
        loadResults(filePath);
    }

    public Translator(List<TranslationEntry> expressionsAndTranslations, String languageFrom, String languageTo) {
        this(languageFrom, languageTo);
        translationEntries = expressionsAndTranslations;
    }

    private Translator(String languageFrom, String languageTo) {
        this.languageFrom = languageFrom;
        this.languageTo = languageTo;
        this.translationEntries = new ArrayList<>();
    }

    public List<TranslationEntry> translate(TranslationSource translationSource) {
        Log.detail("Translating " + translationEntries.size() + " expressions");
        Log.dump(translationEntries);

        List<TranslationSource> translationHandlers = translationEntries.parallelStream()
                .map(translation -> translationSource.createTranslator(translation, languageFrom, languageTo))
                .collect(Collectors.toList());

        for(TranslationSource handler: translationHandlers) {
            try {
                handler.join();
            } catch (Exception e) {
                Log.error("Unknown error on translation.Translator for expression " + handler.getExpression());
                e.printStackTrace();
            }
        }

       return translationEntries;
    }

    public List<TranslationEntry> getTranslationEntries() {
        return translationEntries;
    }

    public List<String> getExpressionsNotTranslated() {

        return translationEntries.stream()
                .filter(translation -> translation.getTranslationsGenerated().isEmpty())
                .map(TranslationEntry::getOriginalExpression)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void filter(List<String> expressionsExcluded) {
        expressionsExcluded.forEach(expressionExcluded ->
                translationEntries.removeIf(translation -> translation.getOriginalExpression().equals(expressionExcluded)));

        Log.info(translationEntries.size() + " expressions kept.");
    }

    private void printStatistics(ArrayList<String> expressionsExcluded, int nTranslations) {
        // expressions not excluded
        List<TranslationEntry> expressionsSubset = translationEntries.stream()
                .filter(translation -> !expressionsExcluded.contains(translation.getOriginalExpression()))
                .collect(Collectors.toList());

        expressionsSubset.forEach(translation -> translation.keepNTranslations(nTranslations));

        int expressionsCount = expressionsSubset.size();

        int WikidataConcept = (int) expressionsSubset.stream().filter(translationEntry ->
                (translationEntry.isTranslationCorrect()
                || translationEntry.isTranslationClose()
                || translationEntry.isTranslationRelated()) && translationEntry.hasWikidataConcept()).count();

        int WikipediaConcept = (int) expressionsSubset.stream().filter(translationEntry ->
                (translationEntry.isTranslationCorrect()
                        || translationEntry.isTranslationClose()
                        || translationEntry.isTranslationRelated()) && translationEntry.hasWikipediaArticle()).count();

        expressionsSubset.removeIf(translation -> translation.getTranslationsGenerated().isEmpty());
        expressionsSubset.forEach(TranslationEntry::evaluate);
        int translatedCount = expressionsSubset.size();

        CSV csvFile = new CSV(languageFrom + "-" + languageTo + Log.getUpTime() + ".csv");
        csvFile.writeLine(Arrays.asList("BLEU", "TER"));
        expressionsSubset.forEach(translationEntry -> {
            csvFile.writeLine(Arrays.asList("" + translationEntry.getTranslationMetrics().getBleuScore(),
                    "" + translationEntry.getTranslationMetrics().getTerScore()));
        });

        // calculate avg quality metrics
        double avgBleu = expressionsSubset.stream()
                .mapToDouble(translationEntry -> translationEntry.getTranslationMetrics().getBleuScore())
                .average().orElse(0);

        double avgTer = expressionsSubset.stream()
                .mapToDouble(translationEntry -> translationEntry.getTranslationMetrics().getTerScore())
                .average().orElse(0);

        List<TranslationEntry> correctlyTranslated = expressionsSubset.stream()
                .filter(TranslationEntry::isTranslationCorrect).collect(Collectors.toList());

        int correctlyTranslatedCount = correctlyTranslated.size();

        expressionsSubset.removeAll(correctlyTranslated);
        List<TranslationEntry> closelyTranslated = expressionsSubset.stream()
                .filter(TranslationEntry::isTranslationClose)
                .collect(Collectors.toList());

        int closelyTranslatedCount = closelyTranslated.size();

        expressionsSubset.removeAll(closelyTranslated);
        List<TranslationEntry> relatedTranslation =expressionsSubset.stream()
                .filter(TranslationEntry::isTranslationRelated)
                .collect(Collectors.toList());

        int relatedTranslatedCount = relatedTranslation.size();

        int ambiguousCount = (int) expressionsSubset.stream().filter(translationEntry -> {
            for (Translation translation: translationEntry.getTranslationsGenerated()) {
                if (translation.getObservations() != null && translation.getObservations().contains(WikidataTranslation.DISAMBIGUATION_PAGE)) {
                    return true;
                }
            }

            return false;
        }).count();

        expressionsSubset.removeAll(relatedTranslation);
        Log.info(expressionsSubset.toString());

        Log.info(translatedCount + "/" + expressionsCount + " entries translated");
        Log.info(translatedCount / (float) expressionsCount + " search success rate");
        Log.info(correctlyTranslatedCount + "/" + translatedCount + " entries correctly translated");
        Log.info(correctlyTranslatedCount / (float) translatedCount + " translation success rate");
        Log.info(WikidataConcept  / (float) translatedCount + " entries translated via Wikidata");
        Log.info(WikipediaConcept  / (float) translatedCount + " entries translated via Wikipedia");
        Log.info(closelyTranslatedCount + "/" + translatedCount + " entries closely translated");
        Log.info(relatedTranslatedCount + "/" + translatedCount + " entries translated to related terms");
        Log.info(ambiguousCount + "/" + translatedCount + " entries were ambiguous");
        Log.info((correctlyTranslatedCount + closelyTranslatedCount + relatedTranslatedCount ) / (float) translatedCount + " total translation rate");
        Log.info(avgBleu + " average BLEU");
        Log.info(avgTer + " average TER");
    }

    public void printStatistics() {
        printStatistics(new ArrayList<>(), Integer.MAX_VALUE);
    }

    public void printStatistics(int nTranslations) {
        printStatistics(new ArrayList<>(), nTranslations);
    }

    public void loadResults(String filePath) {
        try {
                InputStream file = new FileInputStream(filePath);
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream (buffer);

            //deserialize the List
            languageFrom = (String) input.readObject();
            languageTo = (String) input.readObject();
            translationEntries = (List<TranslationEntry>)input.readObject();
        } catch(Exception ex){
            Log.error("Unable to open results file.");
            ex.printStackTrace();
        }
    }
    public void saveResults() {
        saveResults(null);
    }

    public void saveResults(String filename) {
        try {
            FileOutputStream fout = new FileOutputStream(filename == null ? languageFrom + "-" + languageTo + Log.getUpTime() : filename);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(languageFrom);
            oos.writeObject(languageTo);
            oos.writeObject(translationEntries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

