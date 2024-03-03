package com.example.youdo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import androidx.core.app.NotificationCompat;

public class StepCounterService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mStepSensor;
    private int mInitialStepCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (mStepSensor != null) {
            mSensorManager.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        loadInitialStepCount(); // load the initial step count
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
                // Ez az első lépésszám esemény a resetelés után.
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
        if (intent != null && intent.hasExtra("resetSteps")) {
            // Itt csak jelezzük, hogy szükség van a resetelésre.
            // A tényleges resetelést az onSensorChanged-ben kell megtenni, mivel itt van friss információnk.
            mInitialStepCount = -1; // Egy speciális jelzőérték, ami jelzi, hogy resetelni kell.
        }
        startForeground(1, buildForegroundNotification());
        return START_STICKY;
    }

    private void saveInitialStepCount(int initialStepCount) {
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("initialStepCount", initialStepCount);
        editor.apply();
    }
    private Notification buildForegroundNotification() {
        String channelId = "stepCounterServiceChannel"; // Értesítési csatorna azonosítója
        String channelName = "Step Counter Service"; // Értesítési csatorna neve
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android Oreo (API 26) és újabb verziókon szükség van értesítési csatorna létrehozására
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Used by the step counter service");
            // Ne felejtsd el regisztrálni az értesítési csatornát a NotificationManager szolgáltatásnál
            notificationManager.createNotificationChannel(channel);
        }

        // Értesítés létrehozása
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Step Counter Running") // Értesítés címe
                .setContentText("Counting your steps in the background") // Értesítés szövege
                .setSmallIcon(R.drawable.ic_launcher_playstore) // Értesítés ikonja, cseréld le a saját ikonodra
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // Értesítés prioritása

        // Értesítés visszaadása
        return builder.build();
    }


}

