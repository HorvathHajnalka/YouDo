package com.example.youdo.Activities;

import static android.Manifest.permission.ACTIVITY_RECOGNITION;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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

import com.example.youdo.Database.dbStepCounter;
import com.example.youdo.R;
import com.example.youdo.HelperServices.StepCounterHelper.StepCounterService;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StepCounterActivity extends AppCompatActivity {

    private int totalSteps;
    private int previewsTotalSteps;
    private ProgressBar progressBar;
    private TextView steps;
    TextView dateText;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private static final int REQUEST_CODE = 100;
    private dbStepCounter db;
    String todayAsString;
    String userId;
    MaterialButton weeklyStatsBtn;
    private DatePickerDialog datePickerDialog;
    String curr_date;
    String todaysDate;
    Bundle extras;

    /*
    Define a BroadcastReceiver to handle incoming intents (broadcasts)

    It first retrieves the current step count from the intent,
    checks the existing steps from the database for today's date,
    calculates the new total steps,
    and updates both the database and the UI accordingly.
     */
    private BroadcastReceiver stepUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Mylogs", "BroadCast receiver started");
            if ("com.example.youdo.STEP_UPDATE".equals(intent.getAction())) {
                int stepsCount = intent.getIntExtra("steps", 0);
                userId = Integer.toString(intent.getIntExtra("userId", 0));

                // make sure we're on the main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String todayAsString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        // existing step count from the database
                        int existingSteps = db.getStepsByDate(userId, todayAsString);

                        // calculating the new total step count
                        int newTotalSteps = existingSteps + (stepsCount - totalSteps);
                        totalSteps = stepsCount; // Updating the totalSteps value with the new total steps

                        // updating the database with the new step count
                        db.addSteps(userId, todayAsString, newTotalSteps);

                        // updating the UI with the new total step count
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

        todayAsString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        progressBar = findViewById(R.id.progressBar);
        steps = findViewById(R.id.steps);
        dateText = findViewById(R.id.dateText);
        weeklyStatsBtn = findViewById(R.id.weeklyStatsBtn);

        extras = getIntent().getExtras();
        if (extras != null) {
            userId = Integer.toString(extras.getInt("userId", -1));
        }

        loadDataFromDatabase();

        Log.d("Mylogs", "The app created: onCreate()");

        updateUI();

        Log.d("Mylogs", "Broadcastreceiver in activity: onCreate()");

        progressBar.setMax(7500);

        todaysDate = getTodaysDate();
        curr_date = todaysDate;

        dateText.setText("Step Counts on "+ curr_date);

        // check for activity recognition permission and request if not granted
        if (ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACTIVITY_RECOGNITION}, 1);
        } else {
            // initiate step counter service
            initiateStepCounterService();
        }

        // permission launcher for notification permission
        notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d("NotificationPermission", "Notification permission granted.");
            } else {
                Toast.makeText(this, "Enable notifications for counting steps in the background.", Toast.LENGTH_SHORT).show();

                Log.d("NotificationPermission", "Notification permission denied.");
            }
        });
        checkAndRequestNotificationPermission();

        // manually add some data for testing purposes


        db.addOrUpdateSteps(userId, "2025-03-26", 6815);
        db.addOrUpdateSteps(userId, "2025-03-25", 5542);
        db.addOrUpdateSteps(userId, "2025-03-24", 2014);
        db.addOrUpdateSteps(userId, "2025-03-23", 8507);
        db.addOrUpdateSteps(userId, "2025-03-22", 7499);
        db.addOrUpdateSteps(userId, "2025-03-21", 54);
        db.addOrUpdateSteps(userId, "2025-03-20", 13);



        initDatePicker();
        weeklyStatsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });


    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String formattedMonth = (month < 10 ? "0" : "") + month;
                String formattedDayOfMonth = (dayOfMonth < 10 ? "0" : "") + dayOfMonth;
                String strDate = year + "-" + formattedMonth + "-" + formattedDayOfMonth;
                curr_date = strDate;
                Log.e("myLog", "main date "+curr_date);
                dateText.setText("Step Counts on "+ curr_date);
                loadStepsForDate(strDate);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // restrict future dates
    }

    // check if notifications are permitted
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // guide the user to the app's notification settings
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Mylogs", "The app is resuming: onResume()");

        loadDataFromDatabase();

        dateText.setText("Step Counts on " + curr_date);

        IntentFilter filter = new IntentFilter("com.example.youdo.STEP_UPDATE");
        filter.addAction("ACTION_NOTIFICATION_CLEARED");
        registerReceiver(stepUpdateReceiver, filter, RECEIVER_EXPORTED);
       }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister the broadcast receiver when the activity is not in the foreground
        unregisterReceiver(stepUpdateReceiver);
        Log.d("Mylogs", "The app is likely going into the background: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Mylogs", "The app is likely going into the background: onStop()");
    }



    // reset the step count when a long press is detected
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


    // reset stepcounts in service
    private void resetStepCountInService() {
        Intent resetIntent = new Intent(this, StepCounterService.class);
        resetIntent.putExtra("resetSteps", true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(resetIntent);
        } else {
            startService(resetIntent);
        }
    }


    private void loadDataFromDatabase() {
        String todayAsString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        totalSteps = db.getStepsByDate(userId, todayAsString);
    }

    // called when the activity is being destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveStepsToDatabase();
        Log.d("Mylogs", "Service stopped: onDestroy()");
    }

    // handles the result from activity recognition for step counting.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initiateStepCounterService();
        } else {
            Toast.makeText(this, "Activity recognition permission required for step counting", Toast.LENGTH_SHORT).show();
        }
    }

    // updates the user interface based on the current step count fetched from the database.
    private void updateUI() {
        loadDataFromDatabase();
        Log.d("Mylogs", "total steps: " + totalSteps);
        Log.d("Mylogs", "prev steps: " + previewsTotalSteps);

        int currentSteps = totalSteps - previewsTotalSteps;
        steps.setText(String.valueOf(currentSteps));
        progressBar.setProgress(currentSteps);
    }

    // updates the shared preferences to reflect the current running state of the step counter service.
    public static void updateServiceState(Context context, boolean isRunning) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("ServiceRunning", isRunning);
        editor.apply();
    }

    // checks if the step counter service is currently running, based on shared preferences.
    private boolean isServiceRunning() {
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ServiceRunning", false);
    }

    // initiates the step counter service if it's not already running.
    private void initiateStepCounterService() {
        if (!isServiceRunning()) {
            Intent serviceIntent = new Intent(this, StepCounterService.class);
            serviceIntent.putExtra("userId", userId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    // saves the current step count to the database for today's date.
    private void saveStepsToDatabase() {

        db.addSteps(userId, todayAsString, totalSteps);
    }

    // returns today's date in the format "yyyy-MM-dd".
    private static String getTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime());
    }

    private void loadStepsForDate(String date) {
        int stepsForDate = db.getStepsByDate(userId, date);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                steps.setText(String.valueOf(stepsForDate));

                progressBar.setProgress(stepsForDate);
            }
        });

        Log.d("loadStepsForDate", "Lépések betöltve a dátumra: " + date + ", Lépések száma: " + stepsForDate);
    }

}