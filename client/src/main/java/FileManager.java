import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private String nickname = "";
    private File histFile;
    private FileWriter fileOut;

    void setNickname(String nick) {

        if (nickname.length() == 0 && nick.length() > 0) {
            fileInit(nick);
            readFile();
        } else if (nick.length() == 0) {
            closeFile();
        }
        nickname = nick;
    }

    void fileInit(String nick) {
        // Проверяем наличие папки
        mkdir();

        // Открываем файл
        histFile = new File("client/hist/hist_" + nick + ".txt");

        touch();
        try {
            fileOut = new FileWriter(histFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mkdir() {
        File dir = new File("client/hist");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        dir = null;
    }

    private void touch() {
        if (!histFile.exists()) {
            try {
                histFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    List<String> readFile() {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(histFile.getCanonicalPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    void append(String msg) {
        try {
            fileOut.append(msg);
            fileOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void closeFile() {
        try {
            fileOut.close();
        } catch (IOException e) {
            System.out.println("Не удалось закрыть потоки ввода-вывода файла");
        }
    }
}
