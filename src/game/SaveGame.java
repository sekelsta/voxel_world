package sekelsta.game;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import sekelsta.engine.file.*;

public class SaveGame extends SaveName {
    private static final byte METADATA_VERSION = 0;

    private long tick;

    public SaveGame(SaveName saveName) {
        super(saveName);
    }

    private SaveGame(String fileName) {
        super(null, fileName);
    }

    public static SaveGame[] loadMetadata() {
        String[] saves = SaveName.listSaveLocations();
        SaveGame[] saveGames = new SaveGame[saves.length];
        for (int i = 0; i < saves.length; ++i) {
            saveGames[i] = load(saves[i]);
        }
        return saveGames;
    }

    private static SaveGame load(String filename) {
        SaveGame saveGame = new SaveGame(filename);
        File file = saveGame.getFile();
        try (FileInputStream fin = new FileInputStream(file)) {
            int version = fin.read();
            if (version != METADATA_VERSION) {
                return null;
            }
            int nameLen = fin.read() << 8;
            nameLen = nameLen | fin.read();
            byte[] nameBytes = new byte[nameLen];
            fin.read(nameBytes);
            saveGame.name = new String(nameBytes, StandardCharsets.UTF_16BE);
            for (int i = 0; i < Long.BYTES; ++i) {
                saveGame.tick = (saveGame.tick << 8) | fin.read();
            }
        }
        catch (IOException e) {
            return null;
        }

        return saveGame;
    }

    public void update(long newTick) {
        this.tick = newTick;
        write();
    }

    public void deleteSave() {
        try {
            FileUtils.deleteDirectory(new File(getPath()));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile() {
        return new File(getPath() + "/world.dat");
    }

    private void write() {
        File file = getFile();
        file.getParentFile().mkdirs();
        try (FileOutputStream fout = new FileOutputStream(file)) {
            fout.write(METADATA_VERSION);
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_16BE);
            fout.write(nameBytes.length >>> 8);
            fout.write(nameBytes.length & 255);
            fout.write(nameBytes);
            for (int i = Long.BYTES - 1; i >= 0; --i) {
                fout.write((byte)(tick >>> (8 * i)));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "SaveGame v" + METADATA_VERSION + ": \"" + name + "\", " + fileName + " " + tick;
    }
}
