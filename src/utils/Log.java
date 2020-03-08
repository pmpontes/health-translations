package utils;

import org.apache.commons.io.output.TeeOutputStream;

import java.io.*;

public class Log {

    private static int counter = 0;
    private static final String LOG_LOCATION = "logs\\";
    private static final String LOG_FILE = "log-$$$.txt";

    private static final long startTime = System.currentTimeMillis();

    private static boolean registerDetails = true;
    private static boolean saveToFile = false;

    public static void registerDetails(boolean active) {
        registerDetails = active;
    }

    public static void saveToFile(boolean save) {
        saveToFile = save;
        if (saveToFile && FileManager.createFolderIfNotExists(LOG_LOCATION)) {
            try {
                String logName = LOG_FILE.replace("$$$", System.currentTimeMillis() + "");
                FileManager.createFile(LOG_LOCATION + logName);
                FileOutputStream logOutputStream = new FileOutputStream(LOG_LOCATION + logName);
                TeeOutputStream outOutputStream = new TeeOutputStream(System.out, logOutputStream);
                TeeOutputStream errOutputStream = new TeeOutputStream(System.err, logOutputStream);
                PrintStream outPrintStream = new PrintStream(outOutputStream, true);
                PrintStream errPrintStream = new PrintStream(errOutputStream, true);
                System.setErr(errPrintStream);
                System.setOut(outPrintStream);
            } catch (FileNotFoundException e) {
                Log.error("Unable to create Log.");
                e.printStackTrace();
                saveToFile = false;
            }
        }
    }

    public static void incCounter() {
        info(++counter + "");
    }

    public static void dump(Object obj) {
        if (registerDetails) {
            System.out.println(" (#)    " + obj.toString());
            System.out.flush();
        }
    }

    public static void error(String msg) {
        System.err.println(" (!)    " + msg);
        System.err.flush();
    }

    public static void info(String msg) {
        System.out.println(" (i)    " + msg);
        System.out.flush();
    }

    public static void detail(String msg) {
        if(registerDetails){
            System.out.println(" (d)    " + msg);
            System.out.flush();
        }
    }

    public static long getUpTime() {
        return System.currentTimeMillis() - startTime;
    }
}