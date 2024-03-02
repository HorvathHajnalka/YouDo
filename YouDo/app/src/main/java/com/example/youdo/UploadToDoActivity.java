package com.example.youdo;

import static java.util.TimeZone.getTimeZone;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


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
            strUserId = String.valueOf(userId);
        }

        MaterialButton addNewTypeBtn = (MaterialButton) findViewById(R.id.newtypebtn);
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
                startActivity(new Intent(UploadToDoActivity.this, NewTypeActivity.class));
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

                    int todoId = db.addToDo(newtodo);
                    newtodo.setTodoId(todoId);

                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(UploadToDoActivity.this);

                    if (account != null) {
                        createAndAddEventToGoogleCalendar(newtodo, account, strToDoName, strToDoDesc, strDate, new EventCallback() {
                            /**
                             * @param eventId
                             */
                            @Override
                            public void onEventAdded(String eventId) {

                                newtodo.setGoogleTodoId(eventId);
                                db.updateToDo(newtodo);

                                // Toast.makeText(UploadToDoActivity.this, "Event updated"+eventId, Toast.LENGTH_LONG).show();
                                // Log success and notify user through UI
                                Log.d("GoogleCalendarEvent", "Event added successfully. ID: " + eventId);

                                runOnUiThread(() -> {
                                    // Toast.makeText(UploadToDoActivity.this, "Event successfully added to Google Calendar", Toast.LENGTH_LONG).show();
                                });


                            }

                            /**
                             *
                             */
                            @Override
                            public void onError() {
                                Log.e("GoogleCalendarEvent", "Failed to add event to Google Calendar");
                                runOnUiThread(() -> {
                                    // Toast.makeText(UploadToDoActivity.this, "Failed to add event to Google Calendar", Toast.LENGTH_LONG).show();
                                });

                            }
                        });
                    }



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

        return year + "/" + getMonthFormat(month) + "/" + dayOfMonth + "  ";
    }

    private String getMonthFormat(int month) {
        if (month == 1) return "Jan";
        if (month == 2) return "Feb";
        if (month == 3) return "Mar";
        if (month == 4) return "Apr";
        if (month == 5) return "May";
        if (month == 6) return "Jun";
        if (month == 7) return "Jul";
        if (month == 8) return "Aug";
        if (month == 9) return "Sep";
        if (month == 10) return "Oct";
        if (month == 11) return "Nov";
        if (month == 12) return "Dec";

        // DEFAULT
        return "Jan";
    }


    private void createAndAddEventToGoogleCalendar(ToDo newtodo, GoogleSignInAccount account, String title, String description, String dateStr, EventCallback callback) {


        // String title = "Test Event";
        // String description = "This is a test event.";

        // Set the start and end time for the event (you may customize these)
        // 'day' format: "YYYY-MM-DD"


        String googleAccountId = account.getId();

        // Convert date string to LocalDate
        LocalDate startDate = LocalDate.parse(dateStr);

        ZonedDateTime startTime = startDate.atStartOfDay(ZoneOffset.UTC);

        startTime = startTime.minusHours(1);

        if (ZoneId.of("Europe/Budapest").getRules().isDaylightSavings(startTime.toInstant())) {
            startTime = startTime.minusHours(1);
        }


        ZonedDateTime endTime = startTime.plusDays(1);

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


        addEventToGoogleCalendar(newtodo, service, title, description, startTime, endTime, new EventCallback() {
            @Override
            public void onEventAdded(String eventId) {


                callback.onEventAdded(eventId);
            }

            @Override
            public void onError() {

                callback.onError();
            }
        });
    }

    private void addEventToGoogleCalendar(ToDo newtodo, com.google.api.services.calendar.Calendar service,
                                          String title, String description,
                                          ZonedDateTime startTime, ZonedDateTime endTime,
                                          EventCallback callback) {
        new AddEventTask(newtodo, service, title, description, startTime, endTime, callback).execute();
    }


    private class AddEventTask extends AsyncTask<Void, Void, String> {
        ToDo newtodo;
        private com.google.api.services.calendar.Calendar service;
        private String title;
        private String description;
        private ZonedDateTime startTime;
        private ZonedDateTime endTime;
        private EventCallback callback;

        public AddEventTask(ToDo newtodo, com.google.api.services.calendar.Calendar service,
                            String title, String description,
                            ZonedDateTime startTime, ZonedDateTime endTime,
                            EventCallback callback) {
            this.newtodo = newtodo;
            this.service = service;
            this.title = title;
            this.description = description;
            this.startTime = startTime;
            this.endTime = endTime;
            this.callback = callback;
        }


        @Override
        protected String doInBackground(Void... params) {
            try {
                Event event = new Event()
                        .setSummary(title)
                        .setDescription(description);

                DateTime start = new DateTime(Date.from(startTime.toInstant()));
                DateTime end = new DateTime(Date.from(endTime.toInstant()));

                event.setStart(new EventDateTime().setDateTime(start));
                event.setEnd(new EventDateTime().setDateTime(end));

                Event createdEvent = service.events().insert("primary", event).execute();
                return createdEvent.getId();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String eventId) {
            super.onPostExecute(eventId);
            if (eventId != null) {
                Toast.makeText(getApplicationContext(), "Event added to your Google Calendar", Toast.LENGTH_LONG).show();
                newtodo.setGoogleTodoId(eventId);
                db.updateToDo(newtodo);

                // String val = String.valueOf(db.getToDoByGoogleId(eventId));


                // Toast.makeText(UploadToDoActivity.this, "Event updated"+ newtodo.getTodoId(), Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getApplicationContext(), "Failed to add even to your Google Calendar", Toast.LENGTH_LONG).show();
            }
        }
    }

    interface EventCallback {
        void onEventAdded(String eventId);

        void onError();
    }
}


