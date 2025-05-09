package com.example.youdo.Activities;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youdo.Database.dbConnectToDo;
import com.example.youdo.Models.ToDo;
import com.example.youdo.R;
import com.example.youdo.HelperServices.ToDoAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class ToDoMainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> permissionLauncher;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;
    FloatingActionButton addTodoBtn;
    FloatingActionButton stepCounterBtn;
    MaterialButton seestatsbtn;
    ImageView logoutBtn;
    ImageView todoDatePickerBtn;
    RecyclerView recyclerView;
    List<ToDo> todoList;
    com.example.youdo.Database.dbConnectToDo dbConnectToDo;
    int userId = -1;
    private DatePickerDialog datePickerDialog;
    String curr_date;
    String todaysDate;
    TextView todoText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_main);

        todaysDate = getTodaysDate();
        curr_date = todaysDate;

        initDatePicker();

        addTodoBtn = findViewById(R.id.addToDobtn);
        stepCounterBtn = findViewById(R.id.stepCounterbtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        MaterialButton seestatsbtn = findViewById(R.id.seestatsbtn);
        recyclerView = findViewById(R.id.todoRecyclerView);
        todoDatePickerBtn = findViewById(R.id.todoDatePickerBtn);
        todoText = findViewById(R.id.todoText);

        // underline text
        String text = "See your statistics";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        seestatsbtn.setText(spannableString);

        seestatsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ToDoMainActivity.this, MyStatsActivity.class);
                i.putExtra("userId", userId);
                startActivity(i);
            }
        });

        // initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // get google user
        googleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

        if (firebaseUser != null) {
            userId = firebaseUser.getUid().hashCode();
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId")) {
                userId = intent.getIntExtra("userId", -1);
                curr_date = getIntent().getExtras().getString("curr_date", "-1");
        }

        dbConnectToDo = new dbConnectToDo(this);

        addTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ToDoMainActivity.this, UploadToDoActivity.class);
                i.putExtra("userId", userId);
                i.putExtra("curr_date", curr_date);
                startActivity(i);
            }
        });

        todoDatePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        stepCounterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ToDoMainActivity.this, StepCounterActivity.class);
                i.putExtra("userId", userId);
                startActivity(i);
            }
        });

        logoutBtn.setOnClickListener(view ->  logout());
        setupPermissionLauncher();
        checkAndRequestGoogleCalendarPermission();

    }
    private void checkAndRequestGoogleCalendarPermission() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null && !account.getGrantedScopes().contains(new Scope("https://www.googleapis.com/auth/calendar"))) {
            GoogleSignIn.requestPermissions(
                    this,
                    1001, // REQUEST_CODE
                    account,
                    new Scope("https://www.googleapis.com/auth/calendar")
            );
        }
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        GoogleSignInAccount updatedAccount = GoogleSignIn.getLastSignedInAccount(this);
                        if (updatedAccount != null && updatedAccount.getGrantedScopes().contains(new Scope("https://www.googleapis.com/auth/calendar"))) {
                            Log.d("GoogleCalendarEvent", "Calendar permission granted!");
                        } else {
                            Log.e("GoogleCalendarEvent", "Calendar permission denied.");
                            Toast.makeText(this, "Google Calendar permission denied!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }


    private void logout() {
        // firebase log out
        firebaseAuth.signOut();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        // google log out
        if (account != null) {
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Toast.makeText(ToDoMainActivity.this, "Successfully logged out from google account", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            });
        } else {
            Toast.makeText(ToDoMainActivity.this, "Successfully logged out", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }
    private void navigateToLogin() {
        Intent intent = new Intent(ToDoMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        // load To-Do items for selected date
        super.onResume();
        if (!curr_date.equals("-1") && curr_date != null && ! curr_date.equals(getTodaysDate())) loadToDosForDate(curr_date);
        else loadToDosForDate(getTodaysDate());
    }

    // initialize DatePicker dialog with current date as default
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1; // Java months are 0-based
                String formattedMonth = (month < 10 ? "0" : "") + month;
                String formattedDayOfMonth = (dayOfMonth < 10 ? "0" : "") + dayOfMonth;
                String strDate = year + "-" + formattedMonth + "-" + formattedDayOfMonth;
                curr_date = strDate;
                Log.e("myLog", "main date "+curr_date);
                loadToDosForDate(strDate);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
    }

    // get today's date in yyyy-MM-dd format
    private static String getTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime());
    }

    // load To-Do items from database for a specific date
    private void loadToDosForDate(String date) {
        todoList = dbConnectToDo.getToDosByUserAndDate(userId, date);
        ToDoAdapter adapter = new ToDoAdapter(todoList, this, curr_date);
        recyclerView.setAdapter(adapter);
        if (! curr_date.equals("-1") && curr_date != null && ! curr_date.equals(getTodaysDate())) {
                todoText.setText(curr_date);
        }else{
            curr_date = getTodaysDate();
            todoText.setText("ToDos for today");
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            GoogleSignInAccount updatedAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (updatedAccount != null && updatedAccount.getGrantedScopes().contains(new Scope("https://www.googleapis.com/auth/calendar"))) {
                Log.d("GoogleCalendarEvent", "Calendar permission granted!");
            } else {
                Log.e("GoogleCalendarEvent", "Calendar permission NOT granted.");
                Toast.makeText(this, "Google Calendar permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
