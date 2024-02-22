// MainService.java
package com.example.projectamio;

import static com.example.projectamio.MainActivity.IOT_URL;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service implements LightHttpRequestTask.IDataListener {

    private static final int PERIOD = 10000;  // Execute every 100 seconds
    private static final int DELTA = 75;
    private static final String TAG = "MainService";
    private Timer timer;

    private List<Data> previousLightDataList = null;

    // Notification
    private static final String CHANNEL_ID = "myCh";
    private static final int NOTIFICATION_ID = 123;

    Notification notification;
    NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Le service MainService a été créé.");
        startTimer();
        createNotificationChannel();
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
        timer.scheduleAtFixedRate(new MyTimerTask(), 0, PERIOD);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDataReceived(List<Data> newLightDataList) {

        // Remplissage de la liste lors du premier appel
        if (previousLightDataList == null) {
            Log.d(TAG, "Premier appel : ");
            previousLightDataList = newLightDataList;
        }
        // Si les données ont bien été récupérés
        else if (newLightDataList != null) {
            // Etat du changement brusque de luminosité
            boolean stateChange = false;
            // Parcours des motes
            for (int i = 0; i < previousLightDataList.size(); i++) {
                double new_delta = Math.abs(newLightDataList.get(i).light - previousLightDataList.get(i).light);
                // Si la différence de luminosité est supérieur à DELTA
                if (new_delta >= DELTA) {
                    stateChange = true;
                    break;
                }
            }

            // Mise à jour de des données
            previousLightDataList = newLightDataList;
            // Si un changement brusque à eu lieu, on notifie l'utilisateur
            if (stateChange) {
                sendNotification();
            }
        } else {
            Log.d(TAG, "Erreur lors de la récupération des données ");
        }
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Exécution de la tâche périodique");
            LightHttpRequestTask task = new LightHttpRequestTask();
            task.setOnDataReceivedListener(MainService.this);
            task.execute(IOT_URL);
        }
    }

    private void sendNotification() {
        int hourOfDay = java.time.LocalTime.now().getHour();

        if (hourOfDay >= 18 - 10 && hourOfDay < 23) {
            try {
                notificationManagerCompat.notify(0, notification);
            }  catch (Exception e) {
                Log.e(TAG, "Erreur lors de la construction et de l'affichage de la notification : " + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "NotificationChannel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        notificationManagerCompat = NotificationManagerCompat.from(this);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.square_red)
                .setContentTitle("Changement d'état détecté")
                .setContentText("Un changement brusque de luminosité a été détecté sur un mote");

        notification = builder.build();
    }


}
