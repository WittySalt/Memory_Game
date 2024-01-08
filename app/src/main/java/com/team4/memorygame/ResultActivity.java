package com.team4.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    String bestScore;
    String currentScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView textBestScore = findViewById(R.id.textBestScoreContent);
        TextView textCurrentScore = findViewById(R.id.textCurrentScoreContent);
        TextView textScoreComment = findViewById(R.id.textScoreComment);

        textBestScore.setText(convertTime(getBestScore()));
        Intent intent = getIntent();
        int currentscore = intent.getIntExtra("currentscore",0);
        textCurrentScore.setText(convertTime(currentscore));

        if (bestScore != null && bestScore.equalsIgnoreCase(currentScore)){
            textScoreComment.setText(getString(R.string.best_score_comment));
        }
        else{
            textScoreComment.setText(getString(R.string.default_score_comment));
        }

    }

    public void onReturnToMainClick(View view){
        startActivity(new Intent(ResultActivity.this, MainActivity.class));
        finish();
    }

    public int getBestScore(){
        SharedPreferences sp = this.getSharedPreferences("highestscore", Activity.MODE_PRIVATE);
        return sp.getInt("highestscore", 0);
    }

    public String convertTime(int intTime){
        int hours = intTime / 3600;
        int minutes = (intTime % 3600) / 60;
        int seconds = intTime % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                hours, minutes, seconds);
    }

}
