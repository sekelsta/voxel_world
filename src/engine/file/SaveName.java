package sekelsta.engine.file;

import java.io.File;
import java.util.*;

public class SaveName {
    private static final int MAX_LENGTH = 256 - 12;
    private static Set<String> reservedFilenames = Set.of(
        "con", "prn", "aux", "nul",
        "com0", "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
        "lpt0", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
    );

    protected String name;
    protected final String fileName;

    public SaveName(String name) {
        if (name.length() > 255) {
            name = name.substring(0, 255);
        }
        this.name = name;
        this.fileName = getFilesystemCompatibleName(name, Arrays.asList(listSaveLocations()));
    }

    protected SaveName(String name, String fileName) {
        this.name = name;
        this.fileName = fileName;
    }

    protected SaveName(SaveName other) {
        this(other.name, other.fileName);
    }

    public static String[] listSaveLocations() {
        DataFolders.getSaveDir().mkdirs();
        return DataFolders.getSaveDir().list();
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return DataFolders.getUserFolder("saves/" + fileName);
    }

    // Note: Cannot use Path functions as those depend on the machine running it.
    // The result should be a valid file name on both Windows and Linux, regardless of what machine we are on now,
    // so that users can transfer saves from one computer to another.
    // See https://stackoverflow.com/questions/60605811/is-there-a-way-to-check-file-paths-in-java-for-windows-paths-specifically
    // This is a bit more restrictive than necessary
    private String getFilesystemCompatibleName(String name, List<String> saves) {
        String lowercase = name.toLowerCase(Locale.US);
        StringBuilder result = new StringBuilder();
        for (char c : lowercase.toCharArray()) {
            if (isAllowed(c)) {
                result.append(c);
            }
        }

        if (result.length() > MAX_LENGTH) {
            result.delete(MAX_LENGTH, result.length());
        }

        if (reservedFilenames.contains(result.toString())) {
            result.append("_0");
        }

        while (saves.contains(result.toString())) {
            int underscore = result.lastIndexOf("_");
            int i = -1;
            try {
                i = Integer.valueOf(result.substring(underscore + 1));
            }
            catch (NumberFormatException e) { }
            if (i < 0) {
                result.append("_0");
            }
            else {
                result.replace(underscore + 1, result.length(), String.valueOf(i + 1));
            }
        }

        if (result.length() == 0 || result.length() > MAX_LENGTH) {
            return getFilesystemCompatibleName(getRandomString(), saves);
        }

        return result.toString();
    }

    private boolean isAllowed(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= '0' && c <= '9')
            || c == ' ' || c == '(' || c == ')' || c == '_';
    }

    private String getRandomString() {
        Random random = new Random();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 16; ++i) {
            int r = random.nextInt(36);
            char c = (char)(r + '0');
            if (r > 9) {
                c = (char)(c + 'a' - '0');
            }
            result.append(c);
        }
        return result.toString();
    }
}
