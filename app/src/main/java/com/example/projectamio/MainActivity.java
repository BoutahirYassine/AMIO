package com.example.projectamio;
// MainActivity.java
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView textView2;
    private ToggleButton toggleButton;
    private CheckBox checkBoxStartAtBoot;


    // Key for SharedPreferences
    private static final String PREF_START_AT_BOOT = "start_at_boot";
    private MyBootBroadcastReceiver myreceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myreceiver = new MyBootBroadcastReceiver();
        IntentFilter filtre = new IntentFilter("android.intent.action.BOOT_COMPLETED");
        registerReceiver(myreceiver, filtre);

        // Find the components
        textView2 = findViewById(R.id.textView2);
        toggleButton = findViewById(R.id.toggleButton1);
        checkBoxStartAtBoot = findViewById(R.id.checkBoxStartAtBoot);

        Button sendRequestButton = findViewById(R.id.sendRequestButton);
        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HttpRequestTask().execute("http://192.168.127.12:8080/iotlab/rest/data/1/light1/last");
            }
        });


        // Set the toggle button listener
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // React to toggle button changes
                if (isChecked) {
                    // Button is ON
                    startMainService();
                } else {
                    // Button is OFF
                    stopMainService();
                }
            }
        });

        // Set the CheckBox listener
        checkBoxStartAtBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // React to checkbox changes
                Log.d(TAG, "Start at boot checkbox state changed: " + isChecked);

                // Save the checkbox state in SharedPreferences
                saveStartAtBootState(isChecked);
            }
        });

        // Retrieve the checkbox state from SharedPreferences and set it
        boolean startAtBoot = getStartAtBootState();
        checkBoxStartAtBoot.setChecked(startAtBoot);


        Log.d(TAG, "L'activité principale a été créée.");


    }

    private void startMainService() {
        // Start the MainService
        Intent serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);

        // Modify TV2 text
        textView2.setText("En cours");
    }

    private void stopMainService() {
        // Stop the MainService
        Intent serviceIntent = new Intent(this, MainService.class);
        stopService(serviceIntent);

        // Modify TV2 text
        textView2.setText("Arrêté");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "L'activité principale a été détruite.");
    }

    // Save the checkbox state in SharedPreferences
    private void saveStartAtBootState(boolean isChecked) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_START_AT_BOOT, isChecked);
        editor.apply();
    }

    // Retrieve the checkbox state from SharedPreferences
    private boolean getStartAtBootState() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(PREF_START_AT_BOOT, false); // Default is false
    }

    private class HttpRequestTask extends AsyncTask<String, Void, List> {
        private List data_list;
        private MyParser parser = new MyParser();
        @Override
        protected List doInBackground(String... params) {
            String apiUrl = params[0];

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStream in = urlConnection.getInputStream();
                    data_list = parser.readJsonStream(in);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data_list;
        }
        protected void onPostExecute(List data_list) {
            Log.d(TAG, "Réponse du serveur : " + data_list);
            String subject = "Données reçues depuis le serveur";
            String body = "Les données reçues sont : " + data_list;
            String recipient = "yassine.boutahir2015@gmail.com";

            // Envoi d'email
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);

            startActivity(Intent.createChooser(emailIntent, "Envoyer l'email via :"));
        }
    }
    }
