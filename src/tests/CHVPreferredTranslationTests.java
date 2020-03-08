package tests;

import chv.CHV;
import translation.TranslationEntry;
import translation.Translator;
import utils.Log;
import wikipedia.WikipediaTranslator;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CHVPreferredTranslationTests {
    public static void main(String[] args) {
        //Log.registerDetails(false);
        Log.saveToFile(true);

        CHV chv = new CHV("C:\\Users\\pedro\\OneDrive\\Ensino Superior\\Health Translations\\PedroPontes\\CHV_flatfiles_all\\CHV_concepts_terms_flatfile_20110204.tsv");
        chv.parse(1000);
        chv.loadTranslations("C:\\Users\\pedro\\OneDrive\\Ensino Superior\\Health Translations\\PedroPontes\\CHV_flatfiles_all\\conceptsCHVpt.txt");
        chv.filterCHVPreferred();

        Translator translationHandler = new Translator(chv.getChvEntries().stream()
                .map(chvEntry -> new TranslationEntry(chvEntry.getExpression(), chvEntry.getTranslatedExpression()))
                .collect(Collectors.toCollection(ArrayList::new)),
                Translator.EN, Translator.PT);

        translationHandler.translate(new WikipediaTranslator());

        translationHandler.filter(chv.getChemicalsAndDrugs(chv.getChvEntries().stream()
                .filter(chvEntry -> translationHandler.getExpressionsNotTranslated()
                        .contains(chvEntry.getChvPreferredName())).collect(Collectors.toList())).stream()
                .map(chvEntry -> chvEntry.getChvPreferredName()).collect(Collectors.toList()));

        translationHandler.printStatistics();

        Log.info(translationHandler.getExpressionsNotTranslated().toString());
        Log.detail(Log.getUpTime() + "ms");
    }
}
