package parallelCorpus;

import bing.BingTranslator;
import translation.TranslationEntry;
import translation.Translator;
import utils.Log;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class PTCorpus {
    private ArrayList<TranslationEntry> corpusEntries;

    public PTCorpus(String filePath){
        this.corpusEntries = new ArrayList<>();
        loadCorpusEntries(filePath);
    }

    public void loadCorpusEntries(String filePath) {
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            for (String line : (Iterable<String>) stream::iterator) {
                parseTranslationEntry(line);
            }
        } catch (Exception e) {
            Log.error("Error parsing the Parallel Corpus file.");
        }
    }

    public boolean exportParallelCorpus(String filePath) {
        Path path = Paths.get(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            corpusEntries.forEach(entry -> {
                try {
                    writer.write(entry.getOriginalExpression() + ";" + entry.getTranslationsGenerated().iterator().next().getTranslationGenerated() + "\n");
                } catch (Exception e) {
                    Log.error("An error occurred while exporting Parallel Corpus");
                    e.printStackTrace();
                }
            });

            return true;
        } catch (Exception e) {
            Log.error("An error occurred while exporting Parallel Corpus");
            e.printStackTrace();
            return false;
        }
    }

    private void parseTranslationEntry(String entry) {
        String[] entryStr = entry.replace("\"", "").replace(";", "\t").split("\t");
        if (entryStr.length > 0) {
            try {
                TranslationEntry newEntry = new TranslationEntry(entryStr[0]);
                if (!newEntry.getOriginalExpression().isEmpty()) {
                    corpusEntries.add(newEntry);
                }
            } catch (Exception e) {
                Log.detail("Error parsing Parallel Corpus file.");
            }
        }
    }

    public static void main(String[] args) {
        PTCorpus ptc = new PTCorpus("PThealthqueries.csv");
        Log.dump(ptc.corpusEntries.stream().mapToInt(e -> e.getOriginalExpression().length()).sum());
        Translator translationHandler = new Translator(ptc.corpusEntries, Translator.PT, Translator.EN);
        translationHandler.translate(new BingTranslator());
        ptc.exportParallelCorpus("PThealthqueriesEN.csv");
    }
}
