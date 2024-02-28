package com.example.youdo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Scope;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.calendar.model.Event;

public class UploadToDoActivity extends AppCompatActivity {


    EditText uploadName, uploadDesc;
    Button datePickerBtn;
    MaterialButton addNewTypeBtn, saveBtn;
    private DatePickerDialog datePickerDialog;

    String strUserId;
    int userId;
    Bundle extras;
    private GoogleServicesHelper googleServicesHelper;
    GoogleSignInAccount googleSignInAccount;
    dbConnectToDo db = new dbConnectToDo(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_do);

        googleServicesHelper = new GoogleServicesHelper(this);
        googleSignInAccount = GoogleServicesHelper.getSignedInAccount(UploadToDoActivity.this);


        initDatePicker();

        uploadName = findViewById(R.id.addToDoName);
        uploadDesc = findViewById(R.id.addToDoDesc);

        userId = -1;
        extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("userId", -1);
            strUserId =  String.valueOf(userId);
        }

        MaterialButton addNewTypeBtn  = (MaterialButton) findViewById(R.id.newtypebtn);
        TextView saveBtn = (TextView) findViewById(R.id.saveTodoBtn);

        datePickerBtn = (Button) findViewById(R.id.datePickerBtn);
        datePickerBtn.setText(getTodaysDate());

        datePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        addNewTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UploadToDoActivity.this,NewTypeActivity.class));
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strToDoName = uploadName.getText().toString();
                String strToDoDesc = uploadDesc.getText().toString();


                // Date picker values
                DatePicker datePicker = datePickerDialog.getDatePicker();
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();

                Calendar startTime = Calendar.getInstance();
                startTime.set(year, month, day, 0, 0);
                Calendar endTime = (Calendar) startTime.clone();
                endTime.add(Calendar.DAY_OF_MONTH, 1); // to-do for all day

                if(strToDoName.isEmpty()) {
                    Toast.makeText(UploadToDoActivity.this, "Empty name!", Toast.LENGTH_SHORT).show();
                } else {
                    ToDo newtodo = new ToDo();
                    newtodo.setName(strToDoName);
                    newtodo.setDescription(strToDoDesc);
                    newtodo.setUserId(userId);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String strDate = dateFormat.format(startTime.getTime());
                    newtodo.setDate(strDate);

                    db.addToDo(newtodo);

                    googleServicesHelper.createGoogleCalendarEvent(googleSignInAccount, strToDoName, strToDoDesc, startTime, endTime, new GoogleServicesHelper.GoogleCalendarEventCallback() {
                        @Override
                        public void onEventCreated(Event event) {
                            runOnUiThread(() -> Toast.makeText(UploadToDoActivity.this, "Event successfully added to Google Calendar", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onEventCreationError(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(UploadToDoActivity.this, "Failed to add event to Google Calendar"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                // Itt logoljuk a hibaüzenetet
                                Log.e("GoogleCalendarError", "Error adding event to Google Calendar: " + e.getMessage());
                            });
                        }
                    });

                    Toast.makeText(UploadToDoActivity.this, "Successfully added to your ToDo list!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UploadToDoActivity.this, ToDoMainActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                }
            }
        });

    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        month = month + 1;

        return makeDateString(day, month, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = makeDateString(dayOfMonth, month, year);
                datePickerBtn.setText(date);

            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, R.style.DatePickerDialogTheme, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());


    }

    private String makeDateString(int dayOfMonth, int month, int year) {

        return year +"/"+ getMonthFormat(month) +"/"+ dayOfMonth + "  ";
    }

    private String getMonthFormat(int month) {
        if(month  == 1) return "Jan";
        if(month  == 2) return "Feb";
        if(month  == 3) return "Mar";
        if(month  == 4) return "Apr";
        if(month  == 5) return "May";
        if(month  == 6) return "Jun";
        if(month  == 7) return "Jul";
        if(month  == 8) return "Aug";
        if(month  == 9) return "Sep";
        if(month  == 10) return "Oct";
        if(month  == 11) return "Nov";
        if(month  == 12) return "Dec";

        // DEFAULT
        return "Jan";
    }


}