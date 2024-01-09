package com.team4.memorygame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    private ArrayList<GameImage> gameImages;

    private int numberOfImagesOpened;
    private ImageView firstImage;
    private ImageView secondImage;
    private int firstImageId;
    private int secondImageId;
    private int score;
    private int maxScore;

    private boolean gameStarted;
    private boolean wrongImagePairIsStillOpen;
    private boolean isFlipping;
    private boolean isProcessing;
    private boolean timerIsRunning;
    private boolean isPaused;
    private int timerSeconds;
    private Button pauseButton;
    private TextView infoTextView;
    private TextView pauseForeground;
    private final int GRID_COLUMNS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // set listener for all buttons
        findViewById(R.id.backButton).setOnClickListener(this);
        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(this);
        infoTextView = findViewById(R.id.textGameHint);
        defaultText();
        pauseForeground = findViewById(R.id.pauseForeground);

        // initialize the score, timer
        score = 0;
        maxScore = 6;
        timerIsRunning = false;
        timerSeconds = 0;

        // set the image relevant like adaptor and get the images
        SharedPreferences Preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        // get the View and get the images
        RecyclerView gameRecyclerView = findViewById(R.id.gameRecyclerView);
        gameImages = GameImage.createGameImageList(this);
        
        GameImagesAdapter adapter = new GameImagesAdapter(gameImages, Preferences.getString("glide", "No").equals("Yes"));
        adapter.setOnItemClickListener(new GameImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // Start timer on first click
                if (!gameStarted) {
                    timerIsRunning = true;
                    isPaused = false;
                    gameStarted = true;
                    pauseButton.setVisibility(View.VISIBLE);
                    startTimer();
                }

                //
                if (isPaused || isFlipping || isProcessing ||
                        itemView.findViewById(R.id.gameImageView).getForeground() == null) {
                    return;
                }

                if (wrongImagePairIsStillOpen) {
                    // just return
                    return;
                }
                // the logic based on different number of already open.
                if (numberOfImagesOpened == 0) {
                    // Clicked on first image
                    firstImage = itemView.findViewById(R.id.gameImageView);
                    // Reveal image
                    flipCard(firstImage);
                    firstImageId = gameImages.get(position).getId();
                    numberOfImagesOpened = 1;
                } else if (numberOfImagesOpened == 1) {
                    // Clicked on second image
                    secondImage = itemView.findViewById(R.id.gameImageView);
                    // Reveal image
                    flipCard(secondImage);
                    secondImageId = gameImages.get(position).getId();
                    isProcessing = true;
                    if (firstImageId == secondImageId) {
                        // Images matched
                        // first update scores
                        updateScore();
                        // then check
                        if (score == maxScore) {
                            // Game ended
                            stopTimer();
                            pauseButton.setEnabled(false);
                            //Save high scores
                            savehighest(timerSeconds);
                            goResultpage(timerSeconds);
                        }
                        else {
                            // Game not yet end
                            matchedText();
                        }
                    }
                    else {
                        // Images did not match
                        wrongImagePairIsStillOpen = true;
                        didNotMatchText();
                        closeBothImagesAfterTwoSeconds();
                    }
                    numberOfImagesOpened = 0;
                    isProcessing = false;
                }
            }
        });
        gameRecyclerView.setAdapter(adapter);
        gameRecyclerView.setLayoutManager(new GridLayoutManager(this, GRID_COLUMNS));
    }

    private void defaultText(){
        infoTextView.setText(R.string.default_game_hint);
    }
    private void didNotMatchText() {
        infoTextView.setText(R.string.non_matching_pair);
    }

    private void matchedText() {
        infoTextView.setText(R.string.matching_pair_found);
    }

/*    private void winGameText() {
        infoTextView.setText(R.string.win_game);
    }*/


    private void closeBothImagesAfterTwoSeconds() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                flipCard(firstImage);
                flipCard(secondImage);
                wrongImagePairIsStillOpen = false;
                defaultText();
            }
        }, 1500);
    }

    public void flipCard(View v) {
        isFlipping = true;
        // the fliping processing will be animate
        if (v.getForeground() != null) {
            v.animate().withLayer().rotationY(90).setDuration(300).withEndAction(
                    // new runnable lamda
                    () -> {
                        // second quarter turn
                        v.setForeground(null);
                        v.setRotationY(-90);
                        v.animate().withLayer().rotationY(0).setDuration(300).start();
                        isFlipping = false;
                    }
            ).start();
        } else {
            v.animate().withLayer().rotationY(-90).setDuration(300).withEndAction(
                    () -> {
                        // second quarter turn
                        v.setForeground(new ColorDrawable(
                                ContextCompat.getColor(GameActivity.this, R.color.teal_200)));
                        v.setRotationY(90);
                        v.animate().withLayer().rotationY(0).setDuration(300).start();
                        isFlipping = false;
                    }
            ).start();
        }

    }

    private void startTimer() {
        final TextView timerTextView = findViewById(R.id.textTimerContent);
        final Handler handler = new Handler();

        // a new thread to calculate the time
        // and it will check the isRunning status when every updates
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = timerSeconds / 3600;
                int minutes = (timerSeconds % 3600) / 60;
                int seconds = timerSeconds % 60;
                @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d:%02d",
                        hours, minutes, seconds);
                timerTextView.setText(time);
                if (timerIsRunning) {
                    timerSeconds++;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }
    private void stopTimer() {
        timerIsRunning = false;
    }
    private void updateScore() {
        score++;
        String textScore = score + " out of 6";
        TextView matchesNum = findViewById(R.id.textGameMatchContent);
        matchesNum.setText(textScore);
    }

    private void goResultpage(int timerSeconds){
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("currentscore", timerSeconds);
        startActivity(intent);
    }

    public void savehighest(int currentscores) {
        // the sharePreference can only save the primitive type, if list, need to concatenate it to a string.
        // here we just store the highest one
        int highestscore = gethighest();
        if (currentscores < highestscore){
            SharedPreferences sp = this.getSharedPreferences("highestscore", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor =  sp.edit();
            editor.putInt("highestscore",currentscores);
            editor.commit();
        }
    }

    public  int gethighest(){
        SharedPreferences sp = this.getSharedPreferences("highestscore", Activity.MODE_PRIVATE);
        return sp.getInt("highestscore",1000000000);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.backButton) {
            // finish method will go back to previous method
            finish();
        } else if (id == R.id.pauseButton) {
            if (isPaused) {
                resumeGame();
            } else {
                pauseGame();
            }
        }
    }

    public void pauseGame() {
        isPaused = true;
        pauseForeground.setVisibility(View.VISIBLE);
        pauseButton.setText(R.string.resume);
        stopTimer();
    }
    public void resumeGame() {
        isPaused = false;
        timerIsRunning = true;
        pauseForeground.setVisibility(View.INVISIBLE);
        pauseButton.setText(R.string.pause);
        startTimer();
    }
}