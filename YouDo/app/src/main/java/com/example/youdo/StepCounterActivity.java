package com.example.youdo;

import static android.Manifest.permission.ACTIVITY_RECOGNITION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

    private int totalSteps = 0;
    private int previewsTotalSteps = 0;
    private ProgressBar progressBar;
    private TextView steps;
    int userId;

    public StepCounterActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        progressBar = findViewById(R.id.progressBar);
        steps = findViewById(R.id.steps);

        resetSteps();
        loadData();

        progressBar.setMax(7500);

        if (ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACTIVITY_RECOGNITION}, 1);
        } else {
            startService(new Intent(this, StepCounterService.class));
        }
    }


    private BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.youdo.STEP_UPDATE".equals(intent.getAction())) {
                // number of steps
                int stepsCount = intent.getIntExtra("steps", 0);
                // daily steps
                int currentSteps = stepsCount - previewsTotalSteps;
                steps.setText(String.valueOf(currentSteps));
                progressBar.setProgress(currentSteps);
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // register the BroadcastReceiver
        IntentFilter stepUpdateIntentFilter = new IntentFilter("com.example.youdo.STEP_UPDATE");
        registerReceiver(stepUpdateReceiver, stepUpdateIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(stepUpdateReceiver);
        Log.d("ActivityLifecycle", "Az alkalmazás valószínűleg háttérbe került: onPause()");

    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ActivityLifecycle", "Az alkalmazás valószínűleg háttérbe került: onStop()");
    }


    private void resetSteps(){
        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StepCounterActivity.this,"Long press to reset steps", Toast.LENGTH_SHORT).show();
            }
        });
        steps.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Reset UI
                previewsTotalSteps = totalSteps;
                steps.setText("0");
                progressBar.setProgress(0);
                savedData();

                // update initialStepCount
                Intent resetIntent = new Intent(StepCounterActivity.this, StepCounterService.class);
                resetIntent.putExtra("resetSteps", true);
                startService(resetIntent);

                return true;
            }
        });
    }


    private void savedData(){
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("previewsTotalSteps", previewsTotalSteps);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        previewsTotalSteps = sharedPreferences.getInt("previewsTotalSteps", 0);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, StepCounterService.class));
        Log.d("ServiceLifecycle", "A szolgáltatás leállítva: onDestroy()");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && permissions.length > 0 && permissions[0].equals(ACTIVITY_RECOGNITION)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(this, StepCounterService.class));
            } else {
                Toast.makeText(this, "Activity recognition permission required for step counting", Toast.LENGTH_SHORT).show();
            }
        }
    }
}