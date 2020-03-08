package parallelCorpus;

import java.util.HashMap;

public class UMLSCorpusEntry {
    public static final String DELIMITER = ";";
    private String id;
    private String expression;
    private HashMap<String, String> translations;

    public UMLSCorpusEntry(String id, String expression) {
        this.id = id;
        this.expression = expression;
    }

    public void addTranslation(String translation, String origin) {
        translations.put(translation, origin);
    }

    public String toFileString() {
        return id + DELIMITER + expression + DELIMITER + "\r\n";
    }
}
