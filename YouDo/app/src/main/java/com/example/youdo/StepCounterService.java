package com.example.youdo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mStepSensor;
    private int mInitialStepCount = 0;
    public static final String CHANNEL_ID = "step_counter_service_channel";
    public static final int NOTIFICATION_ID = 1;
    private static boolean isServiceRunningInForeground = false;
    Context context;
    private dbStepCounter dbStepCounter;
    String deviceId;
    String todayDate;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        deviceId = com.example.youdo.dbStepCounter.getDeviceId();
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        dbStepCounter = new dbStepCounter(getApplicationContext());

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (mStepSensor != null) {
            mSensorManager.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        loadInitialStepCount();


        if (!isServiceRunningInForeground) {
            createNotificationChannel();

            Notification notification = buildForegroundNotification();
            startForeground(NOTIFICATION_ID, notification);
            isServiceRunningInForeground = true;
        }
    }

    private void loadInitialStepCount() {
        // Az aktuális dátum formázása
        String todayAsString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Az eszköz azonosítójának lekérése
        String deviceId = dbStepCounter.getDeviceId();

        // Az adatbázisból való lépésszám lekérése az adott napra
        int storedStepsForToday = dbStepCounter.getStepsByDate(deviceId, todayAsString);

        // A kezdeti lépésszám beállítása az adatbázisban tárolt értékre
        // Ez az érték azt jelenti, hogy mennyi lépést tettünk meg aznap, mielőtt a szolgáltatás elindult volna
        mInitialStepCount = storedStepsForToday;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StepCounterActivity.updateServiceState(this, false);
        mSensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (mInitialStepCount == -1) {
                // Itt az első érték beállítása történik, ami a szenzor által érzékelt összes lépés
                mInitialStepCount = (int) event.values[0];
                saveInitialStepCount(mInitialStepCount);
            }

            int totalStepsSinceReboot = (int) event.values[0];
            int currentStepCount = totalStepsSinceReboot - mInitialStepCount;

            // Frissítse az adatbázist az új lépésszámmal
            // Feltételezve, hogy a deviceId és a mai dátum megszerzése korábban megfelelően történik


            // Itt a lényeg, hogy az addSteps függvényt használjuk a lépésszám frissítésére
            dbStepCounter.addSteps(deviceId, todayDate, currentStepCount);

            // Küldjön szándékot a UI frissítésére
            Intent intent = new Intent("com.example.youdo.STEP_UPDATE");
            intent.putExtra("steps", currentStepCount);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        StepCounterActivity.updateServiceState(this, true);

        Log.d("Mylogs", "BroadCast receiver started in StepCounterService onStart");
        Intent updateIntent = new Intent("com.example.youdo.STEP_UPDATE");
        // Az adatbázisból kell lekérni az aktuális lépésszámot, és azt kell broadcastolni
        int currentSteps = dbStepCounter.getStepsByDate(deviceId,todayDate); // Feltételezve, hogy van ilyen függvényed
        updateIntent.putExtra("steps", currentSteps);
        sendBroadcast(updateIntent);
        Log.d("StepCounterService", "Service started.");
        return START_STICKY;
    }

    private boolean shouldResetStepCounter() {
        // Használja a Service kontextusát közvetlenül
        SharedPreferences prefs = this.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        boolean hasServiceRestarted = prefs.getBoolean("ServiceRestarted", false);
        if (hasServiceRestarted) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("ServiceRestarted", false);
            editor.apply();
        }
        return hasServiceRestarted;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Counter Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH); // Módosítsd ezt a sort

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            Log.d("StepCounterService", "Notification channel created.");

        }
    }

    private void saveInitialStepCount(int initialStepCount) {
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("initialStepCount", initialStepCount);
        editor.apply();
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setSmallIcon(R.drawable.ic_launcher_playstore)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOngoing(true); // Ez teszi az értesítést eltávolíthatatlanná

        Log.d("StepCounterService", "Foreground notification built.");
        return builder.build();
    }
}
