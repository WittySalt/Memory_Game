package com.team4.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{
    Spinner spinner;

    Spinner spinner2;

    Button saveButton;

    Button defaultButton;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        defaultButton = findViewById(R.id.defaultButton);
        defaultButton.setOnClickListener(this);

        spinner = findViewById(R.id.spinner);
        initializeSpinner(spinner, R.array.fetch_choices, "fetch");

        spinner2 = findViewById(R.id.spinner2);
        initializeSpinner(spinner2, R.array.grid_choices, "grid");
    }

    public void initializeSpinner(Spinner spinner, int choicesId, String preferenceKey){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                choicesId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        String preference = sharedPreferences.getString(preferenceKey, null);

        if (preference != null) {
            spinner.setSelection(adapter.getPosition(preference));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.saveButton) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("fetch", spinner.getSelectedItem().toString());
            editor.putString("grid", spinner2.getSelectedItem().toString());
            editor.apply();
            Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else if (id == R.id.defaultButton) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("fetch");
            editor.remove("grid");
            editor.apply();
            Toast.makeText(this, "Settings defaulted!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}