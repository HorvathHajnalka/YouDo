package com.example.youdo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;



public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    TextView newaccountbtn;
    EditText userNameLogIn, passwordLogIn;
    MaterialButton loginbtn, googlebtn;
    dbConnectUser db = new dbConnectUser(this);

    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameLogIn = findViewById(R.id.username);
        passwordLogIn = findViewById(R.id.password);

        loginbtn = findViewById(R.id.loginbtn);
        googlebtn = findViewById(R.id.googlebtn);
        newaccountbtn = findViewById(R.id.newaccount);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR)) // Hozzáférés a naptár API-hoz
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // switch to registerpage
        newaccountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strUserName = userNameLogIn.getText().toString();
                String strPassword = passwordLogIn.getText().toString();

                if (db.checkUser(strUserName, strPassword)) {
                    int userId = db.getUserId(strUserName, strPassword);
                    if (userId != -1) {
                        Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, ToDoMainActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                        finish();
                    } else {

                        Toast.makeText(LoginActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        googlebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (resultCode == RESULT_OK && task.isSuccessful()) {
                GoogleSignInAccount account = null;
                try {
                    account = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
                if (account != null) {
                    String googleAccountId = account.getId();
                    handleSignInResult(task, googleAccountId);
                }
            } else {
                // Kezeljük a sikertelenséget, például logolással vagy Toast üzenettel
                Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask, String googleAccountId) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                Toast.makeText(this, "Successfully signed in with Google: " + account.getEmail(), Toast.LENGTH_SHORT).show();
                createAndAddEventToGoogleCalendar(account);
            }
        } catch (ApiException e) {
            // Handle ApiException
            Toast.makeText(this, "Google Sign In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Handle GoogleApiClient connection failure
        Toast.makeText(this, "Connection to Google Play Services failed", Toast.LENGTH_SHORT).show();
    }

    private void createAndAddEventToGoogleCalendar(GoogleSignInAccount account) {

        String googleAccountId = account.getId();
        // You can customize the event details here
        String title = "Test Event";
        String description = "This is a test event.";

        // Set the start and end time for the event (you may customize these)
        DateTime startTime = new DateTime("2024-03-01T10:00:00Z"); // Example: 10:00 AM UTC
        DateTime endTime = new DateTime("2024-03-01T11:00:00Z");   // Example: 11:00 AM UTC

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
        new AddEventTask(service, title, description, startTime, endTime).execute();
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
                Toast.makeText(LoginActivity.this, "Event added to Google Calendar", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Error adding event to Google Calendar", Toast.LENGTH_SHORT).show();
            }
        }
    }



}
