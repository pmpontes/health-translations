package umls;

import chv.CHV;
import chv.CHVEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import parallelCorpus.ParallelCorpus;
import parallelCorpus.ParallelCorpusEntry;
import utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UMLSCorpusGenerator {
    private CHV chv;
    private final String languageTo;
    private final String languageFrom;
    private ParallelCorpus parallelCorpusFromTo = new ParallelCorpus();
    private ParallelCorpus parallelCorpusToFrom = new ParallelCorpus();

    public UMLSCorpusGenerator(String languageFrom, String languageTo) {
        chv = new CHV("CHV_concepts_terms_flatfile_20110204.tsv");

        this.languageFrom = languageFrom;
        this.languageTo = languageTo;

        parallelCorpusFromTo = new ParallelCorpus(languageFrom, languageTo);

        if (languageFrom != null) {
            parallelCorpusToFrom = new ParallelCorpus(languageTo, languageFrom);
        }
    }

    public void generate() {
        chv.parse();
        chv.filterChemicalsAndDrugs();

        List<UMLSCorpusEntryGenerator> entriesHandlers = chv.getChvEntries().parallelStream()
                .map(UMLSCorpusEntryGenerator::new)
                .collect(Collectors.toList());

        for (UMLSCorpusEntryGenerator handler: entriesHandlers) {
            try {
                handler.join();
            } catch (Exception e) {
                Log.error("Unknown error on UMLSCorpusGenerator for expression " + handler.getExpression());
                e.printStackTrace();
            }
        }
    }

    private class UMLSCorpusEntryGenerator extends Thread {
        private final CHVEntry entry;

        UMLSCorpusEntryGenerator(CHVEntry entry) {
            this.entry = entry;

            this.run();
        }

        @Override
        public void run() {
           if (languageFrom == null) {
                generateFromEnglish();
           } else {
                generateFromTo(languageFrom, languageTo);
           }
        }

        private void generateFromTo(String languageFrom, String languageTo) {
            try {
                List<String> expressionsLanguageFrom = new ArrayList<>();
                List<String> expressionsLanguageTo = new ArrayList<>();

                JSONObject response = UMLSAPIInteractor.getAPIInstance().getFromAPI("rest/content/current/CUI/" + entry.getCui() + "/atoms", "&pageSize=100", true);
                if (response != null && response.has("result")) {
                    JSONArray results = response.getJSONArray("result");
                    results.forEach(result -> {
                        JSONObject obj = (JSONObject) result;
                        if (obj.has("name") && obj.has("language")) {

                            String[] expressions = obj.getString("name").split(";");

                            if (obj.getString("language").equals(languageFrom)) {
                                Arrays.stream(expressions).forEach(expressionsLanguageFrom::add);
                            } else if (obj.getString("language").equals(languageTo)) {
                                Arrays.stream(expressions).forEach(expressionsLanguageTo::add);
                            }
                        }
                    });

                    addCorpusEntries(expressionsLanguageFrom, expressionsLanguageTo, parallelCorpusFromTo);
                    addCorpusEntries(expressionsLanguageTo, expressionsLanguageFrom, parallelCorpusToFrom);
                }
            } catch (Exception e) {
                Log.detail("Error on UMLSCorpusEntryGenerator " + entry.getCui());
            }
        }

        private void addCorpusEntries(List<String> expressionsLanguageFrom, List<String> expressionsLanguageTo,
                                      ParallelCorpus parallelCorpus) {
            expressionsLanguageFrom.forEach(expression -> {
                ParallelCorpusEntry newEntry = new ParallelCorpusEntry();
                newEntry.setTechnicalExpression(expression);
                expressionsLanguageTo.forEach(newEntry::addPopularTranslation);
                parallelCorpus.addParallelCorpusEntry(newEntry);
            });
        }

        private void generateFromEnglish() {
            ParallelCorpusEntry parallelCorpusEntry = new ParallelCorpusEntry();
            parallelCorpusEntry.setTechnicalExpression(entry.getExpression());

            JSONObject response = UMLSAPIInteractor.getAPIInstance().getFromAPI("rest/content/current/CUI/" + entry.getCui() + "/atoms", "&pageSize=100&language=" + languageTo, false);

            Log.dump(response);
            if (response != null && response.has("result")) {
                JSONArray results = response.getJSONArray("result");
                results.forEach(result -> {
                    JSONObject obj = (JSONObject) result;
                    if (obj.has("name")) {
                        String[] translations = obj.getString("name").split(";");
                        Arrays.stream(translations).forEach(parallelCorpusEntry::addPopularTranslation);
                    }
                });

                parallelCorpusFromTo.addParallelCorpusEntry(parallelCorpusEntry);
            }
        }



        public String getExpression() {
            return entry.getExpression();
        }
    }

    private void filterNotTranslated() {
        parallelCorpusFromTo.filterNotTranslated();
        if (parallelCorpusToFrom != null) {
            parallelCorpusToFrom.filterNotTranslated();
        }
    }

    // for UMLS corpus creation
    public static void main(String[] args) {
        UMLSCorpusGenerator corpus = new UMLSCorpusGenerator("ENG", "POR");
        //Log.registerDetails(true);
        corpus.generate();
        corpus.filterNotTranslated();
        corpus.parallelCorpusToFrom.exportParallelCorpus("UMLS-ENG-POR.txt");
        corpus.parallelCorpusFromTo.exportParallelCorpus("UMLS-POR-ENG.txt");
        Log.dump(corpus.parallelCorpusFromTo.getCorpusEntries().size());
    }
}
