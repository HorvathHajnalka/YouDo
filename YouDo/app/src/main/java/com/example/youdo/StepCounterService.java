package com.example.youdo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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

        createNotificationChannel();

        Notification notification = buildForegroundNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void loadInitialStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", MODE_PRIVATE);
        mInitialStepCount = sharedPreferences.getInt("initialStepCount", 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        // Ellenőrizd, hogy van-e 'resetSteps' extra az intentben, és ha igen, kezeld.
        if (intent.getBooleanExtra("resetSteps", false)) {
            // Itt lenne a reset logika
            mInitialStepCount = -1;
        } else {
            createNotificationChannel();
            startForeground(1, getNotification());
        }
        Log.d("StepCounterService", "Service started.");
        return START_STICKY;
    }
    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, StepCounterActivity.class);
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("YouDo Step Counter")
                .setContentText("Your YouDo step counter is active.")
                .setSmallIcon(R.drawable.ic_launcher_playstore)
                .setContentIntent(pendingIntent)
                .build();
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lépésszámláló Szolgáltatás Csatorna",
                    NotificationManager.IMPORTANCE_HIGH); // Módosítsd ezt a sort

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            Log.d("StepCounterService", "Notification channel created.");

        }
    }


    private void saveInitialStepCount(int initialStepCount) {
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", MODE_PRIVATE);
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
                .setCategory(Notification.CATEGORY_SERVICE);

        Log.d("StepCounterService", "Foreground notification built.");
        return builder.build();
    }


}
