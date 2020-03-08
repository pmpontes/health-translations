package parallelCorpus;

import utils.CSV;
import utils.Log;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

public class FullCorpus {
    public static final String ENGLISH = "English";
    public static final String PORTUGUESE = "Portuguese";
    public static final String DANISH = "Danish";
    public static final String GERMAN = "German";
    public static final String SPANISH = "Spanish";
    public static final String FRENCH = "French";
    public static final String ITALIAN = "Italian";
    public static final String DUTCH = "Dutch";

    private List<String> corpiLanguages = Arrays.asList(ENGLISH, PORTUGUESE, DANISH,GERMAN, SPANISH, FRENCH, ITALIAN, DUTCH);
    private List<ParallelCorpusEntry> corpusEntries = new ArrayList<>();

    public void createCorpus() {
        for (String corpusLanguage: corpiLanguages) {
            ParallelCorpus corpus = new ParallelCorpus(ENGLISH, corpusLanguage);
            corpus.retrieveCorpusEntries();
            corpusEntries.addAll(corpus.getCorpusEntries());
        }

        Map<Integer, List<ParallelCorpusEntry>> collect1 = corpusEntries.stream().collect(groupingBy(ParallelCorpusEntry::getIdInt));
        Map<Integer, List<ParallelCorpusEntry>> collect = new TreeMap<>(collect1);

        CSV csvFile = new CSV("full-corpus" + Log.getUpTime() + ".csv");

        csvFile.writeLine(Arrays.asList("id", ENGLISH, "", PORTUGUESE, "", DANISH, "", GERMAN, "", SPANISH, "", FRENCH, "", ITALIAN, "", DUTCH, ""));
        csvFile.writeLine(Arrays.asList("",
                "Technical term", "Popular term",
                "Technical term", "Popular term",
                "Technical term", "Popular term",
                "Technical term", "Popular term",
                "Technical term", "Popular term",
                "Technical term", "Popular term",
                "Technical term", "Popular term",
                "Technical term", "Popular term"));

        collect.forEach((expressionId, corpusEntries) -> {
            ArrayList<String> line = new ArrayList<>();
            line.add(expressionId + "");

            corpusEntries.forEach(entry -> {
                line.add(entry.getTechnicalTranslation());
                line.add(entry.getCSVPopularTranslations());
            });

            csvFile.writeLine(line);
        });
    }

    // for test purposes
    public static void main(String[] args) {
        FullCorpus pcg = new FullCorpus();
        pcg.createCorpus();
        Log.dump(pcg.corpusEntries);
        Log.dump(pcg.corpusEntries.size());
    }
}
