// MyBootBroadcastReceiver.java
package com.example.projectamio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyBootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Boot completed. Starting service...");

            // Vérifier si l'utilisateur a coché l'option correspondante dans les préférences
            if (getStartAtBootState(context)) {
                // Démarrer le service
                Intent serviceIntent = new Intent(context, MainService.class);
                context.startService(serviceIntent);
                Log.d(TAG, "Service started after boot.");
            } else {
                Log.d(TAG, "User did not choose to start the service after boot.");
            }

    }

    // Récupérer l'état de la CheckBox dans SharedPreferences
    private boolean getStartAtBootState(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("start_at_boot", false); // Default is false
    }
}
