package com.example.youdo.HelperServices.StepCounterHelper;

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

import com.example.youdo.Activities.StepCounterActivity;
import com.example.youdo.Database.dbStepCounter;
import com.example.youdo.R;

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
    private String userId;
    String todayDate;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

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

    // loads the initial step count from the database for today's date
    private void loadInitialStepCount() {
        String todayAsString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int storedStepsForToday = dbStepCounter.getStepsByDate(userId, todayAsString);
        mInitialStepCount = storedStepsForToday;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StepCounterActivity.updateServiceState(this, false);
        mSensorManager.unregisterListener(this);
    }

    // handles step sensor changes, updates step count in database, and broadcasts the update
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (mInitialStepCount == -1) {
                // total number of steps detected by the sensor
                mInitialStepCount = (int) event.values[0];
                saveInitialStepCount(mInitialStepCount);
            }

            int totalStepsSinceReboot = (int) event.values[0];
            int currentStepCount = totalStepsSinceReboot - mInitialStepCount;

            dbStepCounter.addSteps(userId, todayDate, currentStepCount);

            Intent intent = new Intent("com.example.youdo.STEP_UPDATE");
            intent.putExtra("steps", currentStepCount);
            sendBroadcast(intent);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // when the accuracy of a sensor changes.
    }

    @Override
    public IBinder onBind(Intent intent) {
        // required for Services that bind to activities or other services.
        return null; // this service does not support binding.
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        StepCounterActivity.updateServiceState(this, true);

        Log.d("Mylogs", "Broadcast receiver started in StepCounterService onStart");
        if (intent != null && intent.hasExtra("userId")) {
            userId = intent.getStringExtra("userId");
        }
        int currentSteps = dbStepCounter.getStepsByDate(userId, todayDate);
        Intent updateIntent = new Intent("com.example.youdo.STEP_UPDATE");
        updateIntent.putExtra("steps", currentSteps);
        sendBroadcast(updateIntent);
        Log.d("StepCounterService", "Service started.");
        return START_STICKY; // ensures service is restarted if killed by the system
    }

    private boolean shouldResetStepCounter() {
        SharedPreferences prefs = this.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        // checks if the service has been restarted
        boolean hasServiceRestarted = prefs.getBoolean("ServiceRestarted", false);
        if (hasServiceRestarted) {
            // resets the flag once checked
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("ServiceRestarted", false);
            editor.apply();
        }
        return hasServiceRestarted;
    }

    private void saveInitialStepCount(int initialStepCount) {
        // saves the initial step count when the service starts
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("initialStepCount", initialStepCount);
        editor.apply();
    }



    private void createNotificationChannel() {
        // checks if the Android version is Oreo (API 26) or higher, since notification channels are not supported in older versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // creates a NotificationChannel with a unique ID, name and importance level
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Counter Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            Log.d("StepCounterService", "Notification channel created.");
        }
    }

    private Notification buildForegroundNotification() {
        // builds the notification that will be shown when the service is running in the foreground
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setSmallIcon(R.drawable.ic_launcher_playstore)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOngoing(true); // makes the notification non-removable unless the service is stopped

        Log.d("StepCounterService", "Foreground notification built.");
        return builder.build(); // Notification object
    }

}
