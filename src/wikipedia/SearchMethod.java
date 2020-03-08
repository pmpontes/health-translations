package wikipedia;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class SearchMethod implements Serializable {

    protected String language;

    public abstract ArrayList<String> search(String expression);

    public void setLanguage(String language) {
        this.language = language;
    }
}