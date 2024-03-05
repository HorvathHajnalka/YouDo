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
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class StepCounterActivity extends AppCompatActivity {

    private int totalSteps;
    private int previewsTotalSteps;
    private ProgressBar progressBar;
    private TextView steps;
    int userId;

    private BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Mylogs", "BroadCast receiver started");
            if ("com.example.youdo.STEP_UPDATE".equals(intent.getAction())) {
                // Lépések száma
                int stepsCount = intent.getIntExtra("steps", 0);
                totalSteps = stepsCount; // Frissítjük a totalSteps értékét

                // Napi lépések
                int currentSteps = totalSteps - previewsTotalSteps;
                Log.d("StepCounterActivity", "BroadCast receiver current steps: " + currentSteps);

                steps.setText(String.valueOf(currentSteps));
                progressBar.setProgress(currentSteps);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        progressBar = findViewById(R.id.progressBar);
        steps = findViewById(R.id.steps);

        resetSteps();
        loadData();

        Log.d("Mylogs", "Az alkalmazás létrehozva: onCreate()");

        updateUI();

        progressBar.setMax(7500);

        if (ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACTIVITY_RECOGNITION}, 1);
        } else {
            initiateStepCounterService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Mylogs", "Az alkalmazás folytatódik: onResume()");

        loadData(); // Először betöltjük az adatokat
        resetSteps();
        updateUI(); // Az updateUI() már tartalmazza a loadData() hívást, így redundáns volt itt meghívni

        // Regisztráljuk a BroadcastReceiver-t
        IntentFilter filter = new IntentFilter("com.example.youdo.STEP_UPDATE");
        registerReceiver(stepUpdateReceiver, filter);
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
            savedData();
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


    private void savedData(){
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("previewsTotalSteps", previewsTotalSteps);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        totalSteps = sharedPreferences.getInt("totalSteps", 0); // Betöltjük a teljes lépésszámot is
        previewsTotalSteps = sharedPreferences.getInt("previewsTotalSteps", 0);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // StepCounterActivity.updateServiceState(this, true);
        // stopService(new Intent(this, StepCounterService.class));
        saveData();
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
        loadData();
        Log.d("Mylogs", "Current steps: " + (totalSteps - previewsTotalSteps));

        int currentSteps = totalSteps - previewsTotalSteps;
        steps.setText(String.valueOf(currentSteps));
        progressBar.setProgress(currentSteps);
    }

    public static void updateServiceState(Context context, boolean isRunning) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("ServiceRunning", isRunning);
        editor.apply();
    }


    private boolean isServiceRunning() {
        SharedPreferences sharedPreferences = getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE);
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
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("totalSteps", totalSteps); // Elmentjük a teljes lépésszámot is
        editor.putInt("previewsTotalSteps", previewsTotalSteps);
        editor.apply();
    }

}