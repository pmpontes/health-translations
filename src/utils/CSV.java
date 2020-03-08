package utils;

import java.io.*;
import java.util.List;

public class CSV {

    private static final char DEFAULT_SEPARATOR = ',';

    private FileOutputStream fileStream;
    private OutputStreamWriter w;


    public CSV(String csvFile) {
        try {
            fileStream = new FileOutputStream(new File(csvFile));
            w =  new OutputStreamWriter(fileStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLine(List<String> values) {
        writeLine(values, DEFAULT_SEPARATOR, ' ');
    }

    public void writeLine(List<String> values, char separators) {
        writeLine(values, separators, ' ');
    }

    private String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    public void writeLine(List<String> values, char separators, char customQuote) {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        try {
            w.append(sb.toString());
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}