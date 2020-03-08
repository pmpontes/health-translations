package tests;

import google.GoogleTranslator;
import gov.nih.nlm.nls.skr.GenericObject;
import ner.ConceptMapperInteractor;
import parallelCorpus.ParallelCorpus;
import translation.TranslationEntry;
import translation.TranslationSource;
import translation.Translator;
import utils.Log;
import wikipedia.WikipediaTranslator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParallelCorpusTranslationTests {

    public static void main(String[] args) {
        Log.saveToFile(true);

        TranslationSource translator = new WikipediaTranslator();
        TranslationSource translator2 = new GoogleTranslator();


        translate(Translator.EN, Translator.FR, translator, true, 10);
        translate(Translator.FR, Translator.EN, translator, true, 10);
        translate(Translator.EN, Translator.IT, translator, true, 10);
        translate(Translator.IT, Translator.EN, translator, true, 10);
        translate(Translator.EN, Translator.DE, translator, true, 10);
        translate(Translator.DE, Translator.EN, translator, true, 10);
        translate(Translator.EN, Translator.ES, translator, true, 10);
        translate(Translator.ES, Translator.EN, translator, true, 10);
        translate(Translator.EN, Translator.PT, translator, true, 10);
        translate(Translator.PT, Translator.DE, translator, true, 10);

        /*Log.info("=======================================================================");
        for (int i = 0; i < translate1.getTranslationEntries().size(); i++) {
            if (translate1.getTranslationEntries().get(i).getTranslationMetrics().getTerScore() <
                    translate2.getTranslationEntries().get(i).getTranslationMetrics().getTerScore()) {
                Log.info(translate1.getTranslationEntries().get(i).toString());
                Log.info(translate2.getTranslationEntries().get(i).toString());
                Log.info("=======================================================================");
            }
        }*/



        Log.info(Log.getUpTime() + "ms");
    }

    private static Translator translate(String languageFrom, String languageTo, TranslationSource translationMethod, boolean run, int limitTranslations) {
        Translator translationHandler = null;
        if (run) {
            ParallelCorpus pc = new ParallelCorpus();
            pc.loadCorpusEntriesCSV("multilingual-glossary.csv", languageFrom.toUpperCase(), languageTo.toUpperCase());

            ArrayList<TranslationEntry> expressionsAndTranslations = new ArrayList<>();
            pc.getCorpusEntries().forEach(entry -> {
                TranslationEntry newEntry = new TranslationEntry(entry.getPopularExpression(), entry.getTechnicalTranslation());
                entry.getPopularTranslations().forEach(newEntry::addCorrectTranslation);
                expressionsAndTranslations.add(newEntry);
            });

            translationHandler = new Translator(expressionsAndTranslations, languageFrom, languageTo);
            translationHandler.translate(translationMethod);
            translationHandler.saveResults("popular-" + languageFrom + "-" + languageTo + translationMethod.getClass().getSimpleName());
        } else {
            translationHandler = new Translator(languageFrom + "-" + languageTo + translationMethod.getClass().getSimpleName());
        }

        translationHandler.printStatistics(limitTranslations);
        return translationHandler;
    }
}
