package com.example.youdo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.button.MaterialButton;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import com.google.api.client.http.javanet.NetHttpTransport;






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


    // google api
    private static final String API_KEY = "AIzaSyD9w0afG7qAhm5zMGDZSYi5WSt3TR7FY7c";

    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_EVENTS};

    private static final String APPLICATION_NAME = "YouDo";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private GoogleAccountCredential credential;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_do);

        googleServicesHelper = new GoogleServicesHelper(this);
        googleSignInAccount = googleServicesHelper.getSignedInAccount(UploadToDoActivity.this);


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

        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

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
                if (hasGoogleCalendarAccess()) {
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

                        db.addToDo(newtodo);
                        addEventToCalendar(newtodo.getName(),newtodo.getDescription(), startTime, endTime);

                        Toast.makeText(UploadToDoActivity.this, "Successfully added to your ToDo list!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadToDoActivity.this, ToDoMainActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(UploadToDoActivity.this, "No access to Google Calendar", Toast.LENGTH_SHORT).show();
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


    private void addEventToCalendar(String eventName,String eventDesc, Calendar startTime, Calendar endTime) {
        try {
            if (eventName == null || eventName.trim().isEmpty()) {
                Log.e("Calendar Event", "Event name is empty or null");
                return;
            }

            GoogleSignInAccount googleSignInAccount = googleServicesHelper.getSignedInAccount(UploadToDoActivity.this);

            if (googleSignInAccount == null ) {
                Log.e("Calendar Event", "No GoogleSignInAccount. Authorization required.");
                // Toast.makeText(UploadToDoActivity.this, "account not found", Toast.LENGTH_SHORT).show();
                // Handle the case where no GoogleSignInAccount is available
                // You might want to redirect the user to sign in or handle the authorization flow
                return;
            }else{
                Log.e("Calendar Event", "account found"+googleSignInAccount.getEmail());
                //bToast.makeText(UploadToDoActivity.this, "account found"+googleSignInAccount.getEmail(), Toast.LENGTH_SHORT).show();
            }

            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    UploadToDoActivity.this, Collections.singleton(CalendarScopes.CALENDAR));

            credential.setSelectedAccount(googleSignInAccount.getAccount());

            com.google.api.services.calendar.Calendar service = getCalendarService();

            Log.i("Calendar Event", "Event Name: " + eventName);

            Event event = new Event();

            event.setSummary(eventName);

            event.setDescription(eventDesc);


            DateTime startDateTime = new DateTime(startTime.getTime());
            EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("CET");
            event.setStart(start);

            DateTime endDateTime = new DateTime(endTime.getTime());
            EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("CET");
            event.setEnd(end);

            // Insert event using the authenticated service
            service.events().insert("primary", event).execute();
            Log.i("Calendar Event", "Event created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Calendar Event", "Error creating event: " + e.getMessage());
        }
    }


    private com.google.api.services.calendar.Calendar getCalendarService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStreamReader clientSecretsReader = new InputStreamReader(
                new FileInputStream("C:/android_projects/YouDo/YouDo/app/resources/client_secrets.json"));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretsReader);

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                Collections.singletonList(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");


    }

    private boolean hasGoogleCalendarAccess() {
        if (googleSignInAccount != null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(CalendarScopes.CALENDAR))
                    .build();

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (GoogleSignIn.hasPermissions(account, new Scope(CalendarScopes.CALENDAR))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}

