package modemmon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

class Log {

    private final Path path;

    Log(String filename) {

        path = Paths.get(filename);
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            System.out.println("Error creating directory:" + e.getMessage());
        }
    }

    void write(String string) {

        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(string);
            writer.flush();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

}
