// MainService.java
package com.example.projectamio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {
    private static final String TAG = "MainService";
    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Le service MainService a été créé.");
        startTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Le service MainService a été démarré en mode sticky.");
        return START_STICKY; // Le service sera redémarré automatiquement en cas d'arrêt inattendu.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
        Log.d(TAG, "Le service MainService a été arrêté.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Ne s'applique pas pour un service sans liaison.
        return null;
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(), 0, 3000); // Execute every 30 seconds
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Tâche périodique exécutée.");
            // Place your periodic task logic here
        }
    }
}
