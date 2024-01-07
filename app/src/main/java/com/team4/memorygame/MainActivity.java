package com.team4.memorygame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ListItemClickListener{

    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    AutoCompleteTextView input;
    ProgressBar progressBar;
    TextView textView;
    TextView helpText;
    Button fetchButton;
    Button startButton;
    Thread imageProcess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set grid size
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        int gridPreferences = Integer.parseInt(sharedPreferences.getString("grid", "3"));

        //Initialize EditText to autocomplete
        input = findViewById(R.id.textInputEditText);
        String[] webseries = getResources().getStringArray(R.array.recommendations);
        //Create adapter for autocomplete EditText view
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, webseries);
        input.setAdapter(adapter);

        //Initialize fetch button
        fetchButton = findViewById(R.id.fetchButton);
        fetchButton.setOnClickListener(this);

        //Initialize recycler view for images
        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, gridPreferences));
        recyclerView.setAdapter(recyclerAdapter);

        //Initialize help text
        helpText = findViewById(R.id.helpText);

        //Initialize progress bar
        progressBar = findViewById(R.id.progressBar);
        //Initialize notification text
        textView = findViewById(R.id.textView);

        //Initialize start button
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Set grid size
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        int gridPreferences = Integer.parseInt(sharedPreferences.getString("grid", "3"));

        //Clear images
        recyclerAdapter.clearImages();
        //Hide progress bar
        progressBar.setVisibility(View.INVISIBLE);
        //Hide notification text
        textView.setVisibility(View.INVISIBLE);
        //Hide help text
        helpText.setVisibility(View.INVISIBLE);
        //Hide & disable start button
        startButton.setVisibility(View.INVISIBLE);
        startButton.setEnabled(false);
        //Set recycler view to grid layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, gridPreferences));
        //Enable fetch button
        fetchButton.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button
        int id = item.getItemId();

        // Interrupt before switching to menu
        if (imageProcess != null) {
            imageProcess.interrupt();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Deletes files on full exit from application
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (int i = 1; i <= 6; i++) {
            File file = new File(dir, "selectedImage" + i + ".jpg");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        if (buttonId == R.id.fetchButton) {
            // Scrape website and populate GridView
            hideKeyboard(this);
            if (input.getText() != null) {
                if (imageProcess != null) {
                    imageProcess.interrupt();
                    try {
                        imageProcess.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                imageProcess = new Thread(new LoadImagesTask(getApplicationContext(), input.getText().toString()));
                imageProcess.start();

            }

        } else if (buttonId == R.id.startButton) {
            // Start button implementation
            Thread imageDownload = new Thread(new SaveImagesTask(getApplicationContext(), recyclerAdapter.getSelectedImages()));
            imageDownload.start();

        }
    }

    public void hideKeyboard(Activity activity) {
        // Hide keyboard after entering text
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onListItemClick(int position) {
        View itemView = recyclerView.findViewHolderForAdapterPosition(position).itemView;
        ImageView imageView = itemView.findViewById(R.id.imageView);
        ImageView tickBox = itemView.findViewById(R.id.tickBox);

        if (imageView.getBackground() == null) {
            imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.red_border));
            tickBox.setVisibility(View.VISIBLE);
        } else {
            imageView.setBackground(null);
            tickBox.setVisibility(View.INVISIBLE);
        }

        if (recyclerAdapter.getSelectedImages().size() == 6) {
            startButton.setEnabled(true);
        } else if (startButton.isEnabled()) {
            startButton.setEnabled(false);
        }
    }
}