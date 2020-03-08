package ner;

import gov.nih.nlm.nls.skr.GenericObject;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Log;

import java.io.File;
import java.util.*;

public class ConceptMapperInteractor {
    private static String UMLSemailAddress = "pedro.martins.pontes@fe.up.pt";
    private static String UMLSuserName = "pedrompontes";
    private static String UMLSpassword = "";

    public static Map<String, Mapping> performNER_UMLSInteractive(String text) {
        GenericObject myIntMMObj = new GenericObject(100, UMLSuserName, UMLSpassword);
        myIntMMObj.setField("Email_Address", UMLSemailAddress);

        StringBuffer buffer = new StringBuffer(text);
        String bufferStr = buffer.toString();
        myIntMMObj.setField("APIText", bufferStr);
        myIntMMObj.setField("KSOURCE", "1516");
        myIntMMObj.setField("COMMAND_ARGS", "-iD -I --JSONf 2");

        try {
            String results = myIntMMObj.handleSubmission();

            return process(results);
        } catch (RuntimeException e) {
            Log.error("Error on request for " + text);
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, Mapping> performNER_UMLSBatch(String filePath) {
        // Instantiate the object for Generic Batch
        GenericObject myGenericObj = new GenericObject(UMLSuserName,UMLSpassword);
        myGenericObj.setField("Email_Address", UMLSemailAddress);
        myGenericObj.setField("SilentEmail", true);
        myGenericObj.setField("SingLine", true);
        myGenericObj.setField("Batch_Command", "metamap -E -I --JSONf 2");

        File inFile = new File(filePath.trim());
        if (inFile.exists()) {
            myGenericObj.setFileField("UpLoad_File", filePath.trim());
        } else {
            Log.error(filePath + " not found.");
            return null;
        }

        String results = "";
        try {
            results = myGenericObj.handleSubmission();
            Log.dump(results);

            return process(results);
        } catch (RuntimeException e) {
            Log.error("Error on request for " + filePath);
            Log.dump(results);
            e.printStackTrace();
            return null;
        }
    }

    private static Map<String, Mapping> process(String results) {
        Map<String, Mapping> allMappings = new HashMap<>();

        JSONObject resultsJson = new JSONObject(results);

        if(resultsJson.has("AllDocuments")) {
            JSONArray allDocuments = resultsJson.getJSONArray("AllDocuments");

            // each document corresponds to a line in the input file
            for (int i = 0; i < allDocuments.length(); i++) {
                JSONObject document = allDocuments.getJSONObject(i).getJSONObject("Document");

                JSONArray utterances = document.getJSONArray("Utterances");
                if (utterances.length() > 0) {
                    JSONObject utterance = utterances.getJSONObject(0);
                    String documentStr = utterance.getString("UttText");
                    JSONArray phrases = utterance.getJSONArray("Phrases");

                    List<Mapping> documentMappings = new ArrayList<>();

                    // possible different phrases
                    for (int j = 0; j < phrases.length(); j++) {
                        List<Mapping> phraseMappings = new ArrayList<>();
                        JSONObject phrase = phrases.getJSONObject(j);
                        JSONArray mappingsJson = phrase.getJSONArray("Mappings");

                        // possible different mappings
                        for (int k = 0; k < mappingsJson.length(); k++) {
                            JSONObject mappingJson = mappingsJson.getJSONObject(k);
                            Mapping mapping = new Mapping(Integer.parseInt(mappingJson.getString("MappingScore")));

                            // different entities associated to the mapping
                            JSONArray mappingCandidates = mappingJson.getJSONArray("MappingCandidates");
                            for (int l = 0; l <mappingCandidates.length(); l++) {
                                JSONObject mappingCandidate = mappingCandidates.getJSONObject(l);

                                mapping.addEntity(new Entity(
                                        Integer.parseInt(mappingCandidate.getString("CandidateScore")),
                                        mappingCandidate.getString("CandidateMatched"),
                                        mappingCandidate.getString("CandidatePreferred")));
                            }

                            phraseMappings.add(mapping);
                        }

                        // select the best mapping for the phrase
                        Optional<Mapping> bestMapping = phraseMappings.stream().max(Mapping::compareTo);
                        if (bestMapping.isPresent()) {
                            documentMappings.add(bestMapping.get());
                        }
                    }

                    // group the mappings and associate it to the document string
                    allMappings.put(documentStr, documentMappings.stream().reduce(Mapping::new).get());
                }
            }
        }

        return allMappings;
    }
}
