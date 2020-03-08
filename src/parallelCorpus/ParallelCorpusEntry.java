package parallelCorpus;

import java.util.ArrayList;

public class ParallelCorpusEntry {
    public static final String DELIMITER = "|";
    private String id;
    private String technicalExpression;
    private ArrayList<String> popularExpressions = new ArrayList<>();
    private String technicalTranslation;
    private ArrayList<String> popularTranslations = new ArrayList<>();

    public String getTechnicalTranslation() {
        return technicalTranslation;
    }

    public void setTechnicalTranslation(String technicalTranslation) {
        this.technicalTranslation = technicalTranslation.trim();
    }

    public String getTechnicalExpression() {
        return technicalExpression;
    }

    public String getPopularExpression() {
        return !popularExpressions.isEmpty() ? popularExpressions.get(0) : technicalExpression;
    }

    public void setTechnicalExpression(String technicalExpression) {
        this.technicalExpression = technicalExpression.trim();
    }

    public void addPopularTranslation(String popularTranslation) {
        if (popularTranslation != null && !popularTranslations.contains(popularTranslation)) {
            popularTranslations.add(popularTranslation.trim());
        }
    }

    public void addPopularExpression(String popularExpression) {
        if (popularExpression != null && !popularExpressions.contains(popularExpression)) {
            popularExpressions.add(popularExpression.trim());
        }
    }

    @Override
    public String toString() {
        return "ParallelCorpusEntry{" +
                "technicalExpression='" + technicalExpression + '\'' +
                ", popularExpressions=" + popularExpressions +
                ", technicalTranslation='" + technicalTranslation + '\'' +
                ", popularTranslations=" + popularTranslations +
                '}';
    }

    public String toFileString() {
        return id + DELIMITER +
                technicalExpression + DELIMITER +
                popularExpressions + "\r\n";
    }

    public String toCorpusString() {
        return technicalExpression + DELIMITER +
                (popularExpressions.isEmpty() ? "" : popularExpressions + DELIMITER) +
                (technicalTranslation == null ? "" : technicalTranslation + DELIMITER) +
                popularTranslations + "\r\n";
    }

    public String getCSVPopularTranslations() {
        StringBuilder csvStr = new StringBuilder();

        for(String popularTranslation: popularTranslations) {
            if (csvStr.length() > 0) {
                csvStr.append(";").append(popularTranslation);
            } else {
                csvStr.append(popularTranslation);
            }
        }

        return csvStr.toString();
    }

    public String getId() {
        return id;
    }

    public Integer getIdInt() {
        return Integer.parseInt(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getPopularTranslations() {
        return popularTranslations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParallelCorpusEntry that = (ParallelCorpusEntry) o;

        return technicalExpression.equals(that.technicalExpression);

    }

    @Override
    public int hashCode() {
        return technicalExpression.hashCode();
    }
}
