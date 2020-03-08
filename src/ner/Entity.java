package ner;

import java.util.Objects;

public class Entity {
    private int score;
    private String expression;
    private String preferredExpression;

    public Entity(int score, String expression, String preferredExpression) {
        this.score = Math.abs(score);
        this.expression = expression.replace("-", "").replace("/", "").toLowerCase().trim();
        this.preferredExpression = preferredExpression.replace("-", "").replace("/", "").toLowerCase().trim();
    }

    public int getScore() {
        return score;
    }

    public String getExpression() {
        return expression;
    }

    public String getPreferredExpression() {
        return preferredExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity entity = (Entity) o;
        return getScore() == entity.getScore() &&
                Objects.equals(getExpression(), entity.getExpression()) &&
                Objects.equals(getPreferredExpression(), entity.getPreferredExpression());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getScore(), getExpression(), getPreferredExpression());
    }

    @Override
    public String toString() {
        return "Entity{" +
                "score=" + score +
                ", expression='" + expression + '\'' +
                ", preferredExpression='" + preferredExpression + '\'' +
                '}';
    }
}
