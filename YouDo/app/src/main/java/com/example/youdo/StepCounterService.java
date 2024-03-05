package com.example.youdo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


import androidx.core.app.NotificationCompat;

public class StepCounterService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mStepSensor;
    private int mInitialStepCount = 0;
    public static final String CHANNEL_ID = "step_counter_service_channel";
    public static final int NOTIFICATION_ID = 1;
    private static boolean isServiceRunningInForeground = false;
    Context context;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
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
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        mInitialStepCount = sharedPreferences.getInt("initialStepCount", 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StepCounterActivity.updateServiceState(this, false);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (mInitialStepCount == -1) {
                mInitialStepCount = (int) event.values[0];
                saveInitialStepCount(mInitialStepCount);
            }
            int totalStepsSinceReboot = (int) event.values[0];
            int currentStepCount = totalStepsSinceReboot - mInitialStepCount;

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

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        StepCounterActivity.updateServiceState(this, true);
        // Ellenőrizd, hogy van-e 'resetSteps' extra az intentben, és ha igen, kezeld.
        if (intent.getBooleanExtra("resetSteps", false)) {
            // Itt lenne a reset logika
            mInitialStepCount = -1;
            saveInitialStepCount(mInitialStepCount); // Mentés az új kezdőértékkel
        } else {
            if (shouldResetStepCounter()) {
                // Amennyiben az alkalmazás/szerviz újraindult, reseteld az mInitialStepCount értéket
                mInitialStepCount = -1;
                saveInitialStepCount(mInitialStepCount);
            }
        }
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
