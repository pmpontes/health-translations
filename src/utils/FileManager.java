package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileManager {

    public static boolean createFolderIfNotExists(String folderPath) {
        if (!Files.isDirectory(Paths.get(folderPath))) {
            try {
                Files.createDirectory(Paths.get(folderPath));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public static void createFile(String filePath) {
        try {
            Files.createFile(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * openFile.
     * @param filePath the path to the file to be opened.
     * @return the file upon success, null otherwise
     */
    public static File openFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.canRead() && file.isFile()) {
                Log.detail("File " + filePath + " opened.");
                return file;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        Log.error("Unable to open " + filePath);
        return null;
    }
}
