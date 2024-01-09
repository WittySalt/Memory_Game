package com.team4.memorygame;
import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
/*
 one gameimage obj reference one image
 activity have a list of this
*/
public class GameImage {
    private int id;
    private String filePath;

    public GameImage(int id, String filePath) {
        this.id = id;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    // Grab all files from pictures directory
    // Extract file path
    // Create two GameImage objects per file with same id
    // Store all objects into ArrayList
    // Shuffle and return ArrayList
    public static ArrayList<GameImage> createGameImageList(Context context) {
        ArrayList<GameImage> gameImages = new ArrayList<>();

        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] filesInDir = dir.listFiles();

        int id = 1;

        for (File file : filesInDir) {
            String filePath = file.getAbsolutePath();
            GameImage gameImage = new GameImage(id, filePath);
            gameImages.add(gameImage);
            gameImages.add(gameImage);
            id++;
        }

        Collections.shuffle(gameImages);

        return gameImages;
    }
}
