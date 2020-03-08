package umls;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UMLSEntry extends Thread {

    public final static ArrayList<String> PHARMACOLOGICAL_SUBSTANCE = new ArrayList<>(Arrays.asList(new String[]{"Pharmacologic Substance"}));
    public final static ArrayList<String> CHEMICAL_STRUCTURES = new ArrayList<>(Arrays.asList(new String[]{"Organic Chemical", "Inorganic Chemical", "Element, Ion, or Isotope"}));
    private final String cui;
    private JSONObject umlsEntry;

    public UMLSEntry(String cui) {
        this.cui = cui;

        this.run();
    }

    @Override
    public void run() {
        try {
            umlsEntry = getUMLSEntry(cui);
        } catch (Exception e) {
            Log.detail("Error obtaining UMLS entry " + cui);
        }
    }

    public boolean isOfSemanticType(List<String> semanticTypes) {
        if (umlsEntry != null) {
            JSONArray entrySemanticTypes = umlsEntry.getJSONObject("result").getJSONArray("semanticTypes");
            for (Object type : entrySemanticTypes) {
                if (semanticTypes.contains(((JSONObject) type).getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<String> getSemanticTypes() {
        List<String> semanticTypes = new ArrayList<>();
        if (umlsEntry != null) {
            JSONArray entrySemanticTypes = umlsEntry.getJSONObject("result").getJSONArray("semanticTypes");
            for (Object type : entrySemanticTypes) {
                semanticTypes.add(((JSONObject) type).getString("name"));
            }
        }

        return semanticTypes;
    }

    public static JSONObject getUMLSEntry(String cui) {
        return UMLSAPIInteractor.getAPIInstance().getFromAPI("rest/content/current/CUI/" + cui);
    }
    // for test purposes only
    public static void main(String[] args) {
        //Log.dump(new UMLS().isChemicalStructure(new UMLS().getUMLSEntry("C0000097")));
        Log.dump(getUMLSEntry("C0000097"));
    }

    public String getCui() {
        return cui;
    }
}
