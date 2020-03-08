package parallelCorpus;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.Log;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Stream;

public class ParallelCorpus {
    private static final String LEMMA_SOURCE_URL = "https://users.ugent.be/~rvdstich/eugloss/multiPAGE_NUMBER.html";
    private static final String TECHNICAL_TERM = "Technical term:";
    private static final String POPULAR_TERM = "Popular term:";

    public static final String ENGLISH = "English";
    public static final String PORTUGUESE = "Portuguese";

    private String langTo = PORTUGUESE;
    private String langFrom = ENGLISH;
    private HashSet<ParallelCorpusEntry> corpusEntries;

    public ParallelCorpus(){
        this.corpusEntries = new HashSet<>();
    }

    public ParallelCorpus(String langFrom, String langTo) {
        this();
        this.langFrom = langFrom;
        this.langTo = langTo;
    }

    public void retrieveCorpusEntries() {
        retrieveCorpusEntries(false);
    }

    public void retrieveCorpusEntries(boolean export) {
        int pageNumber = 1;

        while (parseWebPageContent(LEMMA_SOURCE_URL.replace("PAGE_NUMBER", String.format("%03d", pageNumber++)))) {
            Log.detail("Page " + (pageNumber - 1) + " loaded.");
        }

        if (export) {
            String fileName = (langTo != null ? "parallel-corpus-" + langFrom + "-" + langTo : langFrom) + ".txt";
            exportParallelCorpus(fileName);
        }
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
                    if (langTo != null) {
                        writer.write(entry.toCorpusString().replace("[", "").replace("]", ""));
                    } else {
                        writer.write(entry.toFileString().replace("[", "").replace("]", ""));
                    }
                } catch (Exception e) {
                    Log.error("An error occurred while exporting Parallel Corpus");
                }
            });

            return true;
        } catch (Exception e) {
            Log.error("An error occurred while exporting Parallel Corpus");
            e.printStackTrace();
            return false;
        }
    }

    private boolean parseWebPageContent(String url) {
        Document webPage = getWebPageContent(url);

        if (webPage != null) {
            Elements elements = webPage.select("p");
            for (int elementIndex = 4; elementIndex < elements.size(); elementIndex += 3) {
                parseTranslationEntry(elements.get(elementIndex));
            }
        }

        return webPage != null;
    }

    private static String clearString(String str) {
        str = str.replace("?", "").replace(",", ";");
        if (str.contains("(") && str.contains(")")) {
            String subStr = str.substring(str.indexOf("("), str.indexOf(")"));
            return str.replace(subStr + ")", "").trim();
        }

        return str.replace("/", "").trim();
    }

    private void parseTranslationEntry(Element entry) {
        try {
            String entryStr = entry.html();

            ParallelCorpusEntry newEntry = new ParallelCorpusEntry();
            String id = entryStr.substring(entryStr.indexOf(".html#") + 6, entryStr.indexOf("\"", entryStr.indexOf(".html#")));
            newEntry.setId(id);

            int currentIndex = !entryStr.contains(langFrom) ? 0: entryStr.indexOf(langFrom);
            currentIndex = entryStr.indexOf(TECHNICAL_TERM, currentIndex);
            String enTechnicalExpression = entryStr.substring(entryStr.indexOf("</a>", currentIndex) + 4, entryStr.indexOf("<br>", currentIndex)).trim();
            newEntry.setTechnicalExpression(clearString(enTechnicalExpression));

            currentIndex = entryStr.indexOf(POPULAR_TERM, currentIndex);
            String enPopularExpressionsStr = entryStr.substring(entryStr.indexOf("</a>", currentIndex) + 4, entryStr.indexOf("<br>", currentIndex));
            Arrays.stream(clearString(enPopularExpressionsStr).split("[,;/]"))
                    .map(ParallelCorpus::clearString)
                    .filter(translation -> !translation.isEmpty())
                    .forEach(translation -> newEntry.addPopularExpression(translation.trim()));

            if (langTo != null) {
                currentIndex = !entryStr.contains(langTo) ? 0 : entryStr.indexOf(langTo);
                currentIndex = entryStr.indexOf(TECHNICAL_TERM, currentIndex);
                String technicalTranslation = entryStr.substring(entryStr.indexOf("</a>", currentIndex) + 4, entryStr.indexOf("<br>", currentIndex)).trim();
                newEntry.setTechnicalTranslation(clearString(technicalTranslation));

                currentIndex = entryStr.indexOf(POPULAR_TERM, currentIndex);
                String popularTranslationsStr = entryStr.substring(entryStr.indexOf("</a>", currentIndex) + 4, entryStr.indexOf("<br>", currentIndex));
                Arrays.stream(clearString(popularTranslationsStr).split("[,;/]"))
                        .map(ParallelCorpus::clearString)
                        .filter(translation -> !translation.isEmpty())
                        .forEach(translation -> newEntry.addPopularTranslation(translation.trim()));
            }

            Log.detail(newEntry.toString());
            corpusEntries.add(newEntry);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void parseTranslationEntry(String entry) {
        String[] entryStr = entry.replace(ParallelCorpusEntry.DELIMITER, "\t").split("\t");
        if (entryStr.length == 4) {
            try {
                ParallelCorpusEntry newEntry = new ParallelCorpusEntry();
                newEntry.setTechnicalExpression(entryStr[0]);
                newEntry.setTechnicalTranslation(entryStr[2]);

                Arrays.stream(entryStr[1].split(",")).forEach(newEntry::addPopularExpression);
                Arrays.stream(entryStr[3].split(",")).forEach(newEntry::addPopularTranslation);

                corpusEntries.add(newEntry);
            } catch (Exception e) {
                Log.detail("Error parsing Parallel Corpus file.");
            }
        } else if (entryStr.length == 2) {
            try {
                ParallelCorpusEntry newEntry = new ParallelCorpusEntry();
                newEntry.setTechnicalExpression(entryStr[0]);

                Arrays.stream(entryStr[1].split(",")).forEach(newEntry::addPopularTranslation);

                corpusEntries.add(newEntry);
            } catch (Exception e) {
                Log.detail("Error parsing Parallel Corpus file.");
            }
        } else {
            Log.detail("Error parsing Parallel Corpus file.");
        }
    }

    private Document getWebPageContent(String url) {
        try {
            return Jsoup
                    .connect(url)
                    .ignoreContentType(true)
                    .followRedirects(false)
                    .get();
        } catch (HttpStatusException e1) {
            if (e1.getStatusCode() == 404) {
                Log.detail("Request to " + url + " failed.");
            } else {
                Log.error("Request to " + url + " failed.");
                e1.printStackTrace();
            }
        } catch (Exception e2) {
            Log.detail("Request to " + url + " failed.");
            e2.printStackTrace();
        }

        return null;
    }

    public HashSet<ParallelCorpusEntry> getCorpusEntries() {
        return corpusEntries;
    }

    public void addParallelCorpusEntry(ParallelCorpusEntry parallelCorpusEntry) {
        corpusEntries.add(parallelCorpusEntry);
    }

    // for test purposes
    public static void main(String[] args) {
        /*ParallelCorpus pcg = new ParallelCorpus(PORTUGUESE, ENGLISH);
        pcg.retrieveCorpusEntries();
        //pcg.loadCorpusEntries("test-corpus.txt");
        pcg.exportParallelCorpus("parallel-corpus-Portuguese-English.txt");
        Log.dump(pcg.corpusEntries);
        Log.dump(pcg.corpusEntries.size());*/
        String t = "ydre dele af legemet (f.eks. ben, ansigt)";
        Log.dump(clearString(t));
    }

    public void filterNotTranslated() {
        corpusEntries.removeIf(parallelCorpusEntry -> parallelCorpusEntry.getTechnicalTranslation() == null && parallelCorpusEntry.getPopularTranslations().isEmpty());
    }

    public void loadCorpusEntriesCSV(String filePath, String languageFrom, String languageTo) {
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            Iterator<String> lines = stream.iterator();
            String header1 = lines.next();
            Log.dump(header1);
            String header2 = lines.next();
            Log.dump(header2);
            int languageFromIndex = Arrays.asList(header1.split(",")).indexOf(languageFrom);
            int languageToIndex = Arrays.asList(header1.split(",")).indexOf(languageTo);

            while (lines.hasNext()) {
                String[] entryStr = lines.next().split(",");
                Log.dump(Arrays.toString(entryStr));

                ParallelCorpusEntry newEntry = new ParallelCorpusEntry();
                newEntry.setTechnicalExpression(entryStr[languageFromIndex]);
                newEntry.setTechnicalTranslation(entryStr[languageToIndex]);

                Arrays.stream(entryStr[languageFromIndex + 1].split(";")).forEach(newEntry::addPopularExpression);
                Arrays.stream(entryStr[languageToIndex + 1].split(";")).forEach(newEntry::addPopularTranslation);

                corpusEntries.add(newEntry);
            }
        } catch (Exception e) {
            Log.error("Error parsing the Parallel Corpus file.");
        }
    }
}
