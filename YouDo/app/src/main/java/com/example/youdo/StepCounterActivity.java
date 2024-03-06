package com.example.youdo;

import static android.Manifest.permission.ACTIVITY_RECOGNITION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterActivity extends AppCompatActivity {

    private int totalSteps;
    private int previewsTotalSteps;
    private ProgressBar progressBar;
    private TextView steps;
    int userId;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private static final int REQUEST_CODE = 100;
    private dbStepCounter db;
    String todayAsString;
    String deviceId;

    /*
    private BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Mylogs", "BroadCast receiver started");
            if ("com.example.youdo.STEP_UPDATE".equals(intent.getAction())) {
                // Lépések száma
                int stepsCount = intent.getIntExtra("steps", 0);
                totalSteps = stepsCount; // Frissítjük a totalSteps értékét

                // Mivel UI frissítést kell végezni, győződjünk meg róla, hogy a fő szálon vagyunk
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Napi lépések
                        int currentSteps = totalSteps - previewsTotalSteps;
                        Log.d("StepCounterActivity", "BroadCast receiver current steps: " + currentSteps);

                        db.addSteps(db.getDeviceId(), todayAsString,currentSteps);

                        steps.setText(String.valueOf(currentSteps));
                        progressBar.setProgress(currentSteps);
                    }
                });
            }
        }
    };*/

    private BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Mylogs", "BroadCast receiver started");
            if ("com.example.youdo.STEP_UPDATE".equals(intent.getAction())) {
                // Lépések száma
                int stepsCount = intent.getIntExtra("steps", 0);

                // Mivel UI frissítést kell végezni, győződjünk meg róla, hogy a fő szálon vagyunk
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String todayAsString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        // A meglévő lépésszám lekérése az adatbázisból
                        int existingSteps = db.getStepsByDate(db.getDeviceId(), todayAsString);

                        // Új lépésszám kiszámítása
                        int newTotalSteps = existingSteps + (stepsCount - totalSteps);
                        totalSteps = stepsCount; // Frissítjük a totalSteps értékét az új összes lépésre

                        // Adatbázis frissítése az új lépésszámmal
                        db.addSteps(db.getDeviceId(), todayAsString, newTotalSteps);

                        // UI frissítése az új teljes lépésszámmal
                        steps.setText(String.valueOf(newTotalSteps));
                        progressBar.setProgress(newTotalSteps);
                    }
                });
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        db = new dbStepCounter(this);

        deviceId = dbStepCounter.getDeviceId();
        todayAsString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());


        progressBar = findViewById(R.id.progressBar);
        steps = findViewById(R.id.steps);

        // resetSteps();
        // loadData();
        loadDataFromDatabase();

        Log.d("Mylogs", "Az alkalmazás létrehozva: onCreate()");

        updateUI();

        Log.d("Mylogs", "Broadcastreceiver activityben: onCreate()");


        progressBar.setMax(7500);

        if (ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACTIVITY_RECOGNITION}, 1);
        } else {
            initiateStepCounterService();
        }

        notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Az engedély megadva, kezdd el az értesítések küldését
                Log.d("NotificationPermission", "Értesítési engedély megadva.");
            } else {
                // Az engedély megtagadva, kezelje ennek megfelelően
                Toast.makeText(this, "Enable notifications for counting steps in the background.", Toast.LENGTH_SHORT).show();

                Log.d("NotificationPermission", "Értesítési engedély megtagadva.");
            }
        });
        checkAndRequestNotificationPermission();
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // Értesítések nincsenek engedélyezve, irányítsuk a felhasználót az alkalmazás értesítési beállításaihoz
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                // Android O és felette
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        }
        // Itt folytathatod a többi, értesítésekkel kapcsolatos logikád implementálását
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Mylogs", "Az alkalmazás folytatódik: onResume()");

        // loadData(); // Először betöltjük az adatokat
        loadDataFromDatabase();
        // resetSteps();
        updateUI(); // Az updateUI() már tartalmazza a loadData() hívást, így redundáns volt itt meghívni

        IntentFilter filter = new IntentFilter("com.example.youdo.STEP_UPDATE");
        // filter.addFlags(Intent.FLAG_RECEIVER_NOT_EXPORTED);
        filter.addAction("ACTION_NOTIFICATION_CLEARED");
        registerReceiver(stepUpdateReceiver, filter, RECEIVER_EXPORTED);
        // registerReceiver(stepUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED, Context.RECEIVER_TAKE_PERSISTABLE_UPDATES, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(stepUpdateReceiver);
        Log.d("Mylogs", "Az alkalmazás valószínűleg háttérbe került: onPause()");

    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Mylogs", "Az alkalmazás valószínűleg háttérbe került: onStop()");
    }


    private void resetSteps() {
        steps.setOnClickListener(v -> Toast.makeText(StepCounterActivity.this,"Long press to reset steps", Toast.LENGTH_SHORT).show());

        steps.setOnLongClickListener(v -> {
            previewsTotalSteps = totalSteps;
            steps.setText("0");
            progressBar.setProgress(0);
            saveStepsToDatabase();
            resetStepCountInService();
            return true;
        });
    }


    private void resetStepCountInService() {
        Intent resetIntent = new Intent(this, StepCounterService.class);
        resetIntent.putExtra("resetSteps", true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(resetIntent);
        } else {
            startService(resetIntent);
        }
    }

    /*
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        totalSteps = sharedPreferences.getInt("totalSteps", 0); // Betöltjük a teljes lépésszámot is
        previewsTotalSteps = sharedPreferences.getInt("previewsTotalSteps", 0);
    }*/
    private void loadDataFromDatabase() {
        String todayAsString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        totalSteps = db.getStepsByDate(db.getDeviceId(), todayAsString); // Feltételezve, hogy a deviceId "device1"
        // Itt nincs szükség a previewsTotalSteps változóra, mivel az adatbázis kezeli az összesítést
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        // StepCounterActivity.updateServiceState(this, true);
        // stopService(new Intent(this, StepCounterService.class));
        saveStepsToDatabase();
        Log.d("Mylogs", "A szolgáltatás leállítva: onDestroy()");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initiateStepCounterService();
        } else {
            Toast.makeText(this, "Activity recognition permission required for step counting", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        loadDataFromDatabase();
        Log.d("Mylogs", "total steps: " + totalSteps);
        Log.d("Mylogs", "prev steps: " + previewsTotalSteps);

        int currentSteps = totalSteps - previewsTotalSteps;
        steps.setText(String.valueOf(currentSteps));
        progressBar.setProgress(currentSteps);
    }

    public static void updateServiceState(Context context, boolean isRunning) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("ServiceRunning", isRunning);
        editor.apply();
    }


    private boolean isServiceRunning() {
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ServiceRunning", false);
    }

    private void initiateStepCounterService() {
        if (!isServiceRunning()) {
            Intent serviceIntent = new Intent(this, StepCounterService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    /*
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("totalSteps", totalSteps); // Elmentjük a teljes lépésszámot is
        editor.putInt("previewsTotalSteps", previewsTotalSteps);
        editor.apply();
    }*/

    private void saveStepsToDatabase() {

        db.addSteps(db.getDeviceId(), todayAsString, totalSteps); // Feltételezve, hogy a deviceId "device1"
    }



}