package tests;

import google.GoogleTranslator;
import parallelCorpus.ParallelCorpus;
import translation.TranslationEntry;
import translation.TranslationSource;
import translation.Translator;
import utils.Log;
import wikipedia.WikipediaTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UMLSCorpusTranslationTests {
    private static String[] languages = { Translator.DE, Translator.FR, Translator.ES, Translator.IT, Translator.PT };

    public static void main(String[] args) {
        Log.saveToFile(true);

        //fromEn(new GoogleTranslator());
        fromEn(new WikipediaTranslator());
        toEn(new WikipediaTranslator());
        toEn(new GoogleTranslator());

        Log.info(Log.getUpTime() + "ms");
    }

    private static void fromEn(TranslationSource translationSource) {

        Arrays.stream(languages).forEach(languageTo -> {
            ParallelCorpus pc = new ParallelCorpus();

            pc.loadCorpusEntries("umls-parallel-corpus-en-" + languageTo + ".txt");

            ArrayList<TranslationEntry> expressionsAndTranslations = new ArrayList<>();
            pc.getCorpusEntries().forEach(entry -> {
                TranslationEntry newEntry = new TranslationEntry(entry.getTechnicalExpression());
                entry.getPopularTranslations().forEach(newEntry::addCorrectTranslation);
                expressionsAndTranslations.add(newEntry);
            });

            Translator translationHandler =
                    new Translator(expressionsAndTranslations,
                            Translator.EN, languageTo);

            translationHandler.translate(translationSource);
            translationHandler.printStatistics();
            translationHandler.saveResults();
        });
    }

    private static void toEn(TranslationSource translationSource) {
        Arrays.stream(languages).forEach(languageFrom -> {
            ParallelCorpus pc = new ParallelCorpus();

            pc.loadCorpusEntries("umls-parallel-corpus-" + languageFrom + "-en.txt");


            ArrayList<TranslationEntry> expressionsAndTranslations = new ArrayList<>();
            pc.getCorpusEntries().forEach(entry -> {
                TranslationEntry newEntry = new TranslationEntry(entry.getTechnicalExpression());
                entry.getPopularTranslations().forEach(newEntry::addCorrectTranslation);
                expressionsAndTranslations.add(newEntry);
            });

            Translator translationHandler =
                    new Translator(expressionsAndTranslations,
                            languageFrom, Translator.EN);

            translationHandler.translate(translationSource);
            translationHandler.printStatistics();
            translationHandler.saveResults();
        });
    }
}
