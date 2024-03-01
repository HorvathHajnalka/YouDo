package com.example.youdo;

import static java.util.TimeZone.getTimeZone;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class UploadToDoActivity extends AppCompatActivity {


    EditText uploadName, uploadDesc;
    Button datePickerBtn;
    MaterialButton addNewTypeBtn, saveBtn;
    private DatePickerDialog datePickerDialog;

    String strUserId;
    int userId;
    Bundle extras;
    dbConnectToDo db = new dbConnectToDo(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_do);

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

                    if (strToDoName.isEmpty()) {
                        Toast.makeText(UploadToDoActivity.this, "Empty name!", Toast.LENGTH_SHORT).show();
                    } else {
                        ToDo newtodo = new ToDo();
                        newtodo.setName(strToDoName);
                        newtodo.setDescription(strToDoDesc);
                        newtodo.setUserId(userId);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String strDate = dateFormat.format(startTime.getTime());
                        newtodo.setDate(strDate);

                        Context context;

                        db.addToDo(newtodo);

                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(UploadToDoActivity.this);
                        createAndAddEventToGoogleCalendar(account,strToDoName,strToDoDesc, strDate);

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




private void createAndAddEventToGoogleCalendar(GoogleSignInAccount account, String title, String description, String dateStr) {


    // String title = "Test Event";
    // String description = "This is a test event.";

    // Set the start and end time for the event (you may customize these)
    // 'day' format: "YYYY-MM-DD"


    String googleAccountId = account.getId();


    DateTime startTime= new DateTime(dateStr + "T00:00:00Z"); // Set time to midnight UTC

    // Create a new DateTime object for the end time by adding one day in milliseconds.
    long oneDayInMilliseconds = TimeUnit.DAYS.toMillis(1);
    DateTime endTime = new DateTime(startTime.getValue() + oneDayInMilliseconds);

    // Use GoogleAccountCredential for authentication
    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
            this, Collections.singleton(CalendarScopes.CALENDAR));

    credential.setSelectedAccountName(account.getAccount().name);

    // Build the Calendar service
    com.google.api.services.calendar.Calendar service = null;
    HttpTransport httpTransport = new NetHttpTransport();
    service = new com.google.api.services.calendar.Calendar.Builder(
            httpTransport, new GsonFactory(), credential)
            .setApplicationName("YouDo")
            .build();



    addEventToGoogleCalendar(service, title, description, startTime, endTime);
}

    private void addEventToGoogleCalendar(com.google.api.services.calendar.Calendar service,
                                          String title, String description, DateTime startTime, DateTime endTime) {
        new UploadToDoActivity.AddEventTask(service, title, description, startTime, endTime).execute();
    }

    private class AddEventTask extends AsyncTask<Void, Void, Boolean> {
        private com.google.api.services.calendar.Calendar service;
        private String title;
        private String description;
        private DateTime startTime;
        private DateTime endTime;

        public AddEventTask(com.google.api.services.calendar.Calendar service,
                            String title, String description, DateTime startTime, DateTime endTime) {
            this.service = service;
            this.title = title;
            this.description = description;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Event event = new Event()
                        .setSummary(title)
                        .setDescription(description);

                EventDateTime start = new EventDateTime()
                        .setDateTime(startTime)
                        .setTimeZone("UTC");
                event.setStart(start);

                EventDateTime end = new EventDateTime()
                        .setDateTime(endTime)
                        .setTimeZone("UTC");
                event.setEnd(end);

                service.events().insert("primary", event).execute();
                return true; // Indicates success
            } catch (IOException e) {
                e.printStackTrace();
                return false; // Indicates failure
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(UploadToDoActivity.this, "Event added to Google Calendar", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UploadToDoActivity.this, "Error adding event to Google Calendar", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

