package practice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class FileUtils {
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private FileUtils() {
    }

    public static Optional<String> readLastLine(Path path) {
        try(BufferedReader reader = Files.newBufferedReader(path)) {
            String line = reader.readLine();
            String lastLine;
            do {
                lastLine = line;
            } while((line = reader.readLine()) != null);

            return Optional.ofNullable(lastLine);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static boolean tryWrite(Path path, String text) {
        try {
            Files.writeString(path, text, StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            System.out.println("warn: ");
            return false;
        }
    }

    public static boolean tryCreateFileIfNotExists(Path path) {
        try {
            if(!Files.exists(path)) {
                Files.createFile(path);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
