package com.team4.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

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

        textBestScore.setText(getBestScore());
        textCurrentScore.setText(getCurrentScore());

        if (bestScore != null && bestScore.equalsIgnoreCase(currentScore)){
            textScoreComment.setText(getString(R.string.best_score_comment));
        }
        else{
            textScoreComment.setText(getString(R.string.default_score_comment));
        }

    }

    public String getBestScore(){
        SharedPreferences sp = this.getSharedPreferences("highestscore", Activity.MODE_PRIVATE);
        return sp.getString("highestscore", "00:00:00");
    }

    public String getCurrentScore(){
        // TODO: fix the method using intent
        SharedPreferences sp = this.getSharedPreferences("currentscore", Activity.MODE_PRIVATE);
        return sp.getString("currentscore", "00:00:00");
    }
}
