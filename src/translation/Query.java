package translation;

import ner.ConceptMapperInteractor;
import ner.Entity;
import ner.Mapping;
import utils.Log;
import wikipedia.TranslationMethod;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Query extends TranslationEntry implements Serializable {
    private Mapping mapping;

    public Query(String originalQuery, String translatedQuery) {
        super(originalQuery, translatedQuery);
    }

    public void map() {
        mapping = ConceptMapperInteractor.performNER_UMLSInteractive(originalExpression).get(originalExpression);
    }

    public static void map(List<Query> queries) {
        // save queries to file
        String fileName = System.currentTimeMillis() + ".txt";
        Path path = Paths.get(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            queries.forEach(query -> {
                try {
                    writer.write( query.originalExpression + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            writer.close();
        } catch (Exception e) {
            Log.error("An error occurred while exporting query temp file");
            e.printStackTrace();
        }

        Map<String, Mapping> mappings = ConceptMapperInteractor.performNER_UMLSBatch(fileName);
        try {
            Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            Log.error("An error occurred while deleting query temp file");
            e.printStackTrace();
        }

        queries.forEach(query -> {
            query.mapping = mappings.get(query.originalExpression);
        });
    }

    @Override
    public void generateTranslations(List<TranslationMethod> translationMethods) {
        translationMethods.forEach(translationMethod -> {
            List<Translation> entityTranslations = new ArrayList<>();
            mapping.getEntities().forEach(entity -> {
                ArrayList<Translation> possibleTranslations = translationMethod.translateExpression(entity.getExpression());
                if (possibleTranslations != null && !possibleTranslations.isEmpty()) {
                    entityTranslations.add(possibleTranslations.get(0));
                }
            });

            addTranslationGenerated(entityTranslations.stream().reduce(Translation::new).orElse(null));
        });
    }

    @Override
    public boolean isTranslationClose() {
        return isTranslationClose() || isTranslationRelated();
    }

    @Override
    public String toString() {
        return "Query{" +
                "originalExpression='" + originalExpression + '\'' +
                ", mapping=" + mapping +
                ", translationsGenerated=" + translationsGenerated +
                ", correctTranslations=" + correctTranslations +
                ", translationMetrics=" + translationMetrics +
                '}';
    }
}
