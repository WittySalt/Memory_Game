package com.team4.memorygame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class SaveImagesTask implements Runnable{
    private ArrayList<BitmapDrawable> bitmaps;
    private Context context;
    Handler mHandler = new Handler();

    ProgressBar progressBar;
    TextView textView;
    Button startButton;
    Button fetchButton;

    SaveImagesTask(Context context, ArrayList<BitmapDrawable> bitmaps) {
        this.bitmaps = bitmaps;
        this.context = context;
    }

    @Override
    public void run() {
        startUI();

        for (int i = 0; i < bitmaps.size(); i++) {
            if (Thread.interrupted()) {
                concludeUI(false);
                break;
            }

            updateUI(i + 1);

            if (!save(bitmaps.get(i).getBitmap(), "selectedImage" + (i + 1) + ".jpg")) {
                Thread.currentThread().interrupt();
                concludeUI(false);
                break;
            }
        }

        concludeUI(true);

    }

    public boolean save(Bitmap image, String filename) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(dir, filename);

        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startUI() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                fetchButton.setEnabled(false);
                startButton.setVisibility(View.INVISIBLE);
                progressBar.setProgress(0);
                progressBar.setMax(bitmaps.size());
                progressBar.setVisibility(View.VISIBLE);
                textView.setText("Starting to download images..");
                textView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateUI(int progress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
                textView.setText(String.format(Locale.ENGLISH, "Saving image %d of %d...", progress, bitmaps.size()));
            }
        });

    }

    private void concludeUI(boolean success) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);

                if (success) {
                    Intent intent = new Intent(context, SettingsActivity.class);
                    //Intent intent = new Intent(context, GameActivity.class);
                    context.startActivity(intent);
                } else {
                    startButton.setVisibility(View.VISIBLE);
                    fetchButton.setEnabled(true);
                    Toast.makeText(context, "Downloading of images failed, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

