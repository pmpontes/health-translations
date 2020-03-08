package chv;

public class CHVEntry {
    private String cui;
    private String expression;
    private String translatedExpression = "";
    private String chvPreferredName;
    private String umlsPreferredName;
    private String explanation;
    private boolean umlsPreferred;
    private boolean chvPreferred;
    private boolean disparaged;
    private double frequencyScore;
    private double contextScore;
    private double cuiScore;
    private double comboScore;
    private double modifiedComboScore;
    private String chvStringId;
    private String chvConceptId;

    CHVEntry(String cui, String expression, String chvPreferredName,
             String umlsPreferredName, String explanation,
             boolean umlsPreferred, boolean chvPreferred,
             boolean disparaged, double frequencyScore,
             double contextScore, double cuiScore,
             double comboScore, double modifiedComboScore,
             String chvStringId, String chvConceptId) {
        this.cui = cui;
        this.expression = expression;
        this.chvPreferredName = chvPreferredName;
        this.umlsPreferredName = umlsPreferredName;
        this.explanation = explanation;
        this.umlsPreferred = umlsPreferred;
        this.chvPreferred = chvPreferred;
        this.disparaged = disparaged;
        this.frequencyScore = frequencyScore;
        this.contextScore = contextScore;
        this.cuiScore = cuiScore;
        this.comboScore = comboScore;
        this.modifiedComboScore = modifiedComboScore;
        this.chvStringId = chvStringId;
        this.chvConceptId = chvConceptId;
    }

    @Override
    public String toString() {
        return "CHVEntry{" +
                "cui='" + cui + '\'' +
                ", expression='" + expression + '\'' +
                ", chvPreferredName='" + chvPreferredName + '\'' +
                ", umlsPreferredName='" + umlsPreferredName + '\'' +
                ", explanation='" + explanation + '\'' +
                ", umlsPreferred=" + umlsPreferred +
                ", chvPreferred=" + chvPreferred +
                ", disparaged=" + disparaged +
                ", frequencyScore=" + frequencyScore +
                ", contextScore=" + contextScore +
                ", cuiScore=" + cuiScore +
                ", comboScore=" + comboScore +
                ", modifiedComboScore=" + modifiedComboScore +
                ", chvStringId='" + chvStringId + '\'' +
                ", chvConceptId='" + chvConceptId + '\'' +
                '}';
    }

    public String getTranslatedExpression() {
        return translatedExpression;
    }

    public void setTranslatedExpression(String translatedExpression) {
        this.translatedExpression = translatedExpression;
    }

    public String getCui() {
        return cui;
    }

    public String getExpression() {
        return expression;
    }

    public String getChvPreferredName() {
        return chvPreferredName;
    }

    public String getUmlsPreferredName() {
        return umlsPreferredName;
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean isUmlsPreferred() {
        return umlsPreferred;
    }

    public boolean isChvPreferred() {
        return chvPreferred;
    }

    public boolean isDisparaged() {
        return disparaged;
    }

    public double getFrequencyScore() {
        return frequencyScore;
    }

    public double getContextScore() {
        return contextScore;
    }

    public double getCuiScore() {
        return cuiScore;
    }

    public double getComboScore() {
        return comboScore;
    }

    public double getModifiedComboScore() {
        return modifiedComboScore;
    }

    public String getChvStringId() {
        return chvStringId;
    }

    public String getChvConceptId() {
        return chvConceptId;
    }
}
