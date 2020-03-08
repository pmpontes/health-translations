package tests;

import ner.Entity;
import parallelCorpus.ParallelCorpusEntry;
import translation.*;
import parallelCorpus.ParallelCorpus;
import utils.Log;
import wikipedia.WikipediaTranslator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class QueryTranslationTests {

    public static void main(String[] args) {
        Log.saveToFile(true);

        enToPt(new WikipediaTranslator());
        //ptToEn(new WikipediaTranslator());
        //enToPt(new GoogleTranslator());
        //ptToEn(new GoogleTranslator());

        Log.info(Log.getUpTime() + "ms");
    }

    public static List<Query> loadQueries(String filePath) {
        List<Query> queries = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            for (String line : (Iterable<String>) stream::iterator) {
                try {
                    String[] entryStr = line.replace(";", "\t").split("\t");
                    // TODO change this
                    //queries.add(new Query(entryStr[0], entryStr[1]));
                    queries.add(new Query(entryStr[0], ""));
                } catch (Exception e) {
                    Log.detail("Error parsing Parallel Corpus file.");
                }
            }
        } catch (Exception e) {
            Log.error("Error parsing the Queries file: " + filePath);
        }

        return queries;
    }

    private static void enToPt(TranslationSource translationMethod) {
        List<TranslationEntry> queriesAndTranslations = new ArrayList<>();
        List<Query> queries = loadQueries("queries_small.txt");

        // map queries to entities
        Query.map(queries);
        Log.info("Queries mapped.");
        Log.dump(queries);
        queriesAndTranslations.addAll(queries);

        Translator translationHandler =
                new Translator(queriesAndTranslations, Translator.EN, Translator.PT);

        translationHandler.translate(translationMethod);
        Log.dump(translationHandler.getTranslationEntries());
        //translationHandler.printStatistics(1);
        //translationHandler.saveResults("queries-EN-PT-" + translationMethod.getClass().getSimpleName());
    }

}
