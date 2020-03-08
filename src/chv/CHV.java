package chv;

import umls.UMLSEntry;
import utils.Log;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CHV {

    private static final int CUI_INDEX = 0;
    private static final int EXPRESSION_INDEX = 1;
    private static final int CHV_PREFERRED_NAME_INDEX = 2;
    private static final int UMLS_PREFERRED_NAME_INDEX = 3;
    private static final int EXPLANATION_INDEX = 4;
    private static final int UMLS_PREFERRED_INDEX = 5;
    private static final int CHV_PREFERRED_INDEX = 6;
    private static final int DISPARAGED_INDEX = 7;
    private static final int FREQUENCY_SCORE_INDEX = 8;
    private static final int CONTEXT_SCORE_INDEX = 9;
    private static final int CUI_SCORE_INDEX = 10;
    private static final int COMBO_SCORE_INDEX = 11;
    private static final int MODIFIED_COMBO_SCORE_INDEX = 12;
    private static final int CHV_STRING_ID_INDEX = 13;
    private static final int CHV_CONCEPT_ID_INDEX = 14;
    private static final String TRUE = "yes";
    private static final String NAN = "\\N";
    private static final int CHV_TRANSLATION_EXPRESSION_INDEX = 1;
    private static final int CHV_TRANSLATION_INDEX = 2;
    private static final int CHV_TRANSLATION_VALUE = 3;


    private final String chvFilePath;
    private ArrayList<CHVEntry> chvEntries = new ArrayList<>();

    public CHV(String chvFilePath) {
        this.chvFilePath = chvFilePath;
    }

    public List<CHVEntry> getChvEntries() {
        return chvEntries;
    }

    public void parse() {
        parse(null);
    }

    public void parse(Integer limit) {
        try (Stream<String> stream = Files.lines(Paths.get(chvFilePath))) {
            for (String line : (Iterable<String>) stream::iterator) {
                CHVEntry newEntry = parseCHVEntry(line);

                if (newEntry != null) {
                    chvEntries.add(newEntry);
                } else {
                    Log.error("Error parsing entry " + line);
                }

                if (limit != null && --limit <= 0) {
                    break;
                }
            }

        } catch (Exception e) {
            Log.error("Error parsing the CHV file.");
        } finally {
            Log.info(chvEntries.size() + " CHV entries loaded.");
        }
    }

    public void loadTranslations(String translationsFilePath) {
        try (Stream<String> stream = Files.lines(Paths.get(translationsFilePath))) {
            for (String line : (Iterable<String>) stream::iterator) {
                parseTranslationEntry(line);
            }
        } catch (Exception e) {
            Log.error("Error parsing the CHV Translations file.");
        } finally {
            Log.info(chvEntries.stream().filter(chvEntry -> !chvEntry.getTranslatedExpression().isEmpty()).count() + " CHV translations loaded.");
        }
    }

    private void parseTranslationEntry(String translationEntry) {
        String[] chvTranslation = translationEntry.replace(" |||| ", "\t").split("\t");
        if (chvTranslation.length <= CHV_TRANSLATION_VALUE) {
            return;
        }

        try {
            Optional<CHVEntry> originalEntry = chvEntries.stream()
                    .filter(entry -> entry.getChvPreferredName().compareToIgnoreCase(chvTranslation[CHV_TRANSLATION_EXPRESSION_INDEX]) == 0)
                    .findFirst();

            if (originalEntry.isPresent()){
                originalEntry.get().setTranslatedExpression(chvTranslation[CHV_TRANSLATION_INDEX]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CHVEntry parseCHVEntry(String chvEntry) {
        String[] chvAttributes = chvEntry.split("\t");
        if (chvAttributes.length <= CHV_CONCEPT_ID_INDEX) {
            return null;
        }

        try {
            return new CHVEntry(chvAttributes[CUI_INDEX],
                    chvAttributes[EXPRESSION_INDEX],
                    chvAttributes[CHV_PREFERRED_NAME_INDEX],
                    chvAttributes[UMLS_PREFERRED_NAME_INDEX],
                    chvAttributes[EXPLANATION_INDEX],
                    chvAttributes[UMLS_PREFERRED_INDEX].equals(TRUE),
                    chvAttributes[CHV_PREFERRED_INDEX].equals(TRUE),
                    chvAttributes[DISPARAGED_INDEX].equals(TRUE),
                    chvAttributes[FREQUENCY_SCORE_INDEX].equals(NAN) ? Double.NaN : Double.parseDouble(chvAttributes[FREQUENCY_SCORE_INDEX]),
                    chvAttributes[CONTEXT_SCORE_INDEX].equals(NAN) ? Double.NaN : Double.parseDouble(chvAttributes[CONTEXT_SCORE_INDEX]),
                    chvAttributes[CUI_SCORE_INDEX].equals(NAN) ? Double.NaN : Double.parseDouble(chvAttributes[CUI_SCORE_INDEX]),
                    chvAttributes[COMBO_SCORE_INDEX].equals(NAN) ? Double.NaN : Double.parseDouble(chvAttributes[COMBO_SCORE_INDEX]),
                    chvAttributes[MODIFIED_COMBO_SCORE_INDEX].equals(NAN) ? Double.NaN: Double.parseDouble(chvAttributes[MODIFIED_COMBO_SCORE_INDEX]),
                    chvAttributes[CHV_STRING_ID_INDEX],
                    chvAttributes[CHV_CONCEPT_ID_INDEX]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void filterCHVPreferred() {
        chvEntries.removeIf(chvEntry -> !chvEntry.isChvPreferred());
        Log.info(chvEntries.size() + " CHV entries kept after filterCHVPreferred().");
    }

    public List<CHVEntry> getChemicalsAndDrugs() {
        return getChemicalsAndDrugs(chvEntries);
    }

    public List<CHVEntry> getChemicalsAndDrugs(List<CHVEntry> chvEntries) {
        List<String> semanticTypes = Stream.concat(UMLSEntry.CHEMICAL_STRUCTURES.stream(), UMLSEntry.PHARMACOLOGICAL_SUBSTANCE.stream()).collect(Collectors.toList());
        ArrayList<String> chemicalsAndDrugs = new ArrayList<>();
        List<UMLSEntry> entriesHandlers = chvEntries.parallelStream()
                .map(chvEntry -> new UMLSEntry(chvEntry.getCui()))
                .collect(Collectors.toList());

        for (UMLSEntry handler: entriesHandlers) {
            try {
                handler.join();
                if (handler.isOfSemanticType(semanticTypes)) {
                    chemicalsAndDrugs.add(handler.getCui());
                }
            } catch (Exception e) {
                Log.error("Unknown error on CHV for expression " + handler.getCui());
                e.printStackTrace();
            }
        }

        return chvEntries.parallelStream().filter(chvEntry -> chemicalsAndDrugs.contains(chvEntry.getCui())).collect(Collectors.toList());
    }

    public void filterChemicalsAndDrugs() {
        chvEntries.removeAll(getChemicalsAndDrugs());
    }

    // for test purposes only
    public static void main(String[] args) {
        CHV t = new CHV("G:\\Documents\\GitHub\\Health-Translator\\CHV_concepts_terms_flatfile_20110204.tsv");
        t.parse();
        t.filterChemicalsAndDrugs();
        t.printDomains();
    }

    private void printDomains() {
        HashMap<String, Integer> domains = new HashMap<>();
        List<UMLSEntry> entriesHandlers = chvEntries.parallelStream()
                .map(chvEntry -> new UMLSEntry(chvEntry.getCui()))
                .collect(Collectors.toList());

        for (UMLSEntry handler: entriesHandlers) {
            try {
                handler.join();
                handler.getSemanticTypes().forEach(semanticType -> {
                    domains.put(semanticType, domains.containsKey(semanticType) ? domains.get(semanticType) + 1 : 1);
                });
            } catch (Exception e) {
                Log.error("Unknown error on CHV for expression " + handler.getCui());
                e.printStackTrace();
            }
        }

        domains.forEach((domain, count) -> Log.info(domain + "=" + count / (double) entriesHandlers.size()));
    }
}
