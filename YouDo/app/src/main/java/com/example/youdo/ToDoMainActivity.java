package com.example.youdo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ToDoMainActivity extends AppCompatActivity {

    FloatingActionButton addTodoBtn;
    FloatingActionButton stepCounterBtn;
    ImageView todoDatePickerBtn;
    RecyclerView recyclerView;
    List<ToDo> todoList;
    dbConnectToDo dbConnectToDo;
    int userId = -1; // has to be updated
    private DatePickerDialog datePickerDialog;
    String curr_date;
    String todaysDate;
    TextView todoText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_main);

        // Initialize current and today's date
        todaysDate = getTodaysDate();
        curr_date = todaysDate;

        // Setup DatePicker dialog
        initDatePicker();

        // Link UI elements with their IDs
        addTodoBtn = findViewById(R.id.addToDobtn);
        stepCounterBtn = findViewById(R.id.stepCounterbtn);
        recyclerView = findViewById(R.id.todoRecyclerView);
        todoDatePickerBtn = findViewById(R.id.todoDatePickerBtn);
        todoText = findViewById(R.id.todoText);

        // Receive userId and curr_date from previous activity if provided
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId")) {
            userId = intent.getIntExtra("userId", -1); // -1 if we don't know the "userId"
            curr_date = getIntent().getExtras().getString("curr_date", "-1");
        }

        // Initialize database connection
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
    }
    @Override
    protected void onResume() {
        // Load To-Do items for selected date
        super.onResume();
        if (!curr_date.equals("-1") && curr_date != null && ! curr_date.equals(getTodaysDate())) loadToDosForDate(curr_date);
        else loadToDosForDate(getTodaysDate());
    }

    // Initialize DatePicker dialog with current date as default
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

    // Helper method to get today's date in yyyy-MM-dd format
    private static String getTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime());
    }

    // Load To-Do items from database for a specific date
    private void loadToDosForDate(String date) {
        todoList = dbConnectToDo.getToDosByUserAndDate(userId, date);
        ToDoAdapter adapter = new ToDoAdapter(todoList, this, curr_date);
        recyclerView.setAdapter(adapter);
        // Update the text view to show selected date or indicate "ToDos for today"
        if (! curr_date.equals("-1") && curr_date != null && ! curr_date.equals(getTodaysDate())) {
                todoText.setText(curr_date);
        }else{
            curr_date = getTodaysDate();
            todoText.setText("ToDos for today");
        }
        adapter.notifyDataSetChanged();
    }

}
