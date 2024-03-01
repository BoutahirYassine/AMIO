package com.example.projectamio;
// MainActivity.java
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LightHttpRequestTask.IDataListener {

    private static final String TAG = "MainActivity";
    private TextView textView2;
    private ToggleButton toggleButton;
    private CheckBox checkBoxStartAtBoot;


    // Key for SharedPreferences
    private static final String PREF_START_AT_BOOT = "start_at_boot";
    private MyBootBroadcastReceiver myreceiver;
    private static final String IOT_URL = "http://iotlab.telecomnancy.eu:8080/iotlab/rest/data/1/light1/last";

    private List<Data> manualLightDataList = null;

    private Vibrator vibrator;


    @Override
    public void onDataReceived(List<Data> dataList) {
        // Mise à jour des données
        String dataTextView="";
        TextView text=findViewById(R.id.textView);
        manualLightDataList = dataList;
       for(int i=0;i<3;i++) {
           dataTextView += "----\nmote : " + dataList.get(i).mote + "\nData : " + dataList.get(i).light.toString() + "\ntimestamp : " + dataList.get(i).timestamp.toString() + "\n";       }
        text.setText(dataTextView);

        sendMail();
    }

    private void sendMail() {
        int hourOfDay = java.time.LocalTime.now().getHour();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int heure_deb=Integer.parseInt(sharedPreferences.getString("heure_debut","0"));
        int heure_fin=Integer.parseInt(sharedPreferences.getString("heure_fin","0"));
        Log.d("heure",sharedPreferences.getString("heure_debut","0"));
        if (hourOfDay >= heure_deb && hourOfDay < heure_fin) {

            String subject = "Données reçues depuis le serveur";
            String body = "Les données reçues sont : " + manualLightDataList;
            String recipient = "yassine.boutahir2015@gmail.com";

            // Envoi d'email
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            vibratePhone(500);
            startActivity(Intent.createChooser(emailIntent, "Envoyer l'email via :"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        myreceiver = new MyBootBroadcastReceiver();
        IntentFilter filtre = new IntentFilter("android.intent.action.BOOT_COMPLETED");
        registerReceiver(myreceiver, filtre);

        // demande de permission de notification
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},99);
        // Find the components
        textView2 = findViewById(R.id.textView2);
        toggleButton = findViewById(R.id.toggleButton1);

        Button sendRequestButton = findViewById(R.id.sendRequestButton);
        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Send request button");
                LightHttpRequestTask task = new LightHttpRequestTask(MainActivity.this);
                task.setOnDataReceivedListener(MainActivity.this);
                task.execute(IOT_URL);
            }
        });

        //partir vers la page des préférences
        Button settingsbutton = findViewById(R.id.button2);
        settingsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
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

        Log.d(TAG, "L'activité principale a été créée.");
    }

    private void startMainService() {
        Log.d(TAG, "START MAIN SERVICE");
        // Start the MainService
        Intent serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);

        // Modify TV2 text
        textView2.setText("Service en cours");
    }

    private void stopMainService() {
        // Stop the MainService
        Intent serviceIntent = new Intent(this, MainService.class);
        stopService(serviceIntent);

        // Modify TV2 text
        textView2.setText("Service arrêté");
    }

    private void vibratePhone(long milliseconds) {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(milliseconds);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vibrator != null) {
            vibrator.cancel();
        }
        Log.d(TAG, "L'activité principale a été détruite.");
    }

}
