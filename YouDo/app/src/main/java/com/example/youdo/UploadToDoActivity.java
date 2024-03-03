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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;


public class UploadToDoActivity extends AppCompatActivity {


    EditText uploadName, uploadDesc;
    Button datePickerBtn;
    MaterialButton addNewTypeBtn, saveBtn;
    private DatePickerDialog datePickerDialog;
    TextView mainTitle;

    String strUserId;
    int userId;
    int editTodoId;
    Bundle extras;
    dbConnectToDo db = new dbConnectToDo(this);
    ToDo editTodo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_do);

        initDatePicker();

        uploadName = findViewById(R.id.addToDoName);
        uploadDesc = findViewById(R.id.addToDoDesc);
        mainTitle = findViewById(R.id.mainTitle);
        addNewTypeBtn = findViewById(R.id.newtypebtn);
        saveBtn = findViewById(R.id.saveTodoBtn);
        datePickerBtn = findViewById(R.id.datePickerBtn);


        extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("userId", -1);
            strUserId = String.valueOf(userId);
            editTodoId = extras.getInt("todoId", -1);
        }

        if (editTodoId != -1) {
            editTodo = db.getTodoById(editTodoId);
            mainTitle.setText("Edit ToDo");
            uploadName.setText(editTodo.getName());
            uploadDesc.setText(editTodo.getDescription());

            // convert from yyyy-mm-dd to yyyy/mm/dd

            String originalDateString = editTodo.getDate();
            String formattedDateString = "";

            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd  ");

            try {
                Date date = originalFormat.parse(originalDateString);
                formattedDateString = targetFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            datePickerBtn.setText(formattedDateString);

        } else {
            datePickerBtn.setText(getTodaysDate());
        }

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


                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(UploadToDoActivity.this);

                    int todoId = -1;

                    // new to do
                    if (editTodoId == -1) {
                        ToDo newtodo = new ToDo();
                        newtodo.setName(strToDoName);
                        newtodo.setDescription(strToDoDesc);
                        newtodo.setUserId(userId);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String strDate = dateFormat.format(startTime.getTime());
                        newtodo.setDate(strDate);

                        todoId = db.addToDo(newtodo);
                        newtodo.setTodoId(todoId);

                        Toast.makeText(UploadToDoActivity.this, "Successfully added to your ToDo list!", Toast.LENGTH_SHORT).show();

                        if (account != null) {
                            createAndAddEventToGoogleCalendar(newtodo, account, strToDoName, strToDoDesc, strDate, new EventCallback() {
                                /**
                                 * @param eventId
                                 */
                                @Override
                                public void onEventAdded(String eventId) {
                                    newtodo.setGoogleTodoId(eventId);
                                    db.updateToDo(newtodo);
                                    Log.d("GoogleCalendarEvent", "Event added successfully. ID: " + eventId);
                                }

                                @Override
                                public void onError() {
                                    Log.e("GoogleCalendarEvent", "Failed to add event to Google Calendar");
                                }
                            });
                        }
                    }
                    // edit old to do
                    else {
                        editTodo.setName(strToDoName);
                        editTodo.setDescription(strToDoDesc);
                        editTodo.setUserId(userId);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String strDate = dateFormat.format(startTime.getTime());
                        editTodo.setDate(strDate);

                        boolean updated = db.updateToDo(editTodo);
                        if (updated) {
                            Toast.makeText(UploadToDoActivity.this, "ToDo has been updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UploadToDoActivity.this, "ToDo update failed!", Toast.LENGTH_SHORT).show();
                        }

                        String eventId = editTodo.getGoogleTodoId();
                        if (account != null && eventId != null) {
                            CompletableFuture.runAsync(() -> {
                                try {
                                    updateEvent(account, eventId, strToDoName, strToDoDesc, strDate);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).thenRun(() -> {
                                // UI frissítése vagy más tevékenység, ami a hívás befejezése után szükséges
                                // Ezt a kódot a fő szálon kell futtatni, pl. használhatod a runOnUiThread ha Activity-ből hívod

                            });
                            Toast.makeText(UploadToDoActivity.this, "ToDo has been updated in Google Calendar!", Toast.LENGTH_SHORT).show();
                        }

                    }


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

    // update event
    private void updateEvent(GoogleSignInAccount account, String eventId, String strToDoName, String strToDoDesc, String strDate) {
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
        if (service == null) {
            Log.w("UpdateEvent", "Google Calendar API service not initialized.");
            return;
        }

        try {
            // Find the event
            Event event = service.events().get("primary", eventId).execute();

            // Update the event details
            event.setSummary(strToDoName);
            event.setDescription(strToDoDesc);

            LocalDate startDate = LocalDate.parse(strDate);

            ZonedDateTime startTime = startDate.atStartOfDay(ZoneId.of("Europe/Budapest"));
            ZonedDateTime endTime = startTime.plusDays(1);

            // Adjust start and end time if necessary, considering daylight saving
            EventDateTime start = new EventDateTime().setDateTime(new DateTime(startTime.toInstant().toString())).setTimeZone("Europe/Budapest");
            event.setStart(start);

            EventDateTime end = new EventDateTime().setDateTime(new DateTime(endTime.toInstant().toString())).setTimeZone("Europe/Budapest");
            event.setEnd(end);

            // Save the event
            event = service.events().update("primary", event.getId(), event).execute();

            Log.d("UpdateEvent", "Event updated: " + event.getHtmlLink());


        } catch (Exception e) {
            Log.e("UpdateEvent", "Exception occurred: " + e.getMessage());
        }
    }
}


