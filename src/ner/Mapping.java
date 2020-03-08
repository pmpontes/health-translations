package ner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Mapping implements Comparable{
    private List<Entity> entities = new ArrayList<>();
    private int score;

    public Mapping(int score) {
        this.score = Math.abs(score);
    }

    public Mapping(Mapping mapping) {
        this.score = mapping.score;
        this.entities = mapping.entities;
    }

    public Mapping(Mapping mapping1, Mapping mapping2) {
        this.score = Math.abs((mapping1.score + mapping2.score)/2);
        this.entities.addAll(mapping1.entities);
        this.entities.addAll(mapping2.entities);
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    @Override
    public int compareTo(Object o) {
        return score - ((Mapping) o).score;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "entities=" + entities +
                ", score=" + score +
                '}';
    }
}
