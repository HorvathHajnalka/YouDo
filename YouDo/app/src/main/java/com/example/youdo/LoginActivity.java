package com.example.youdo;

import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.api.services.calendar.CalendarScopes;



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

        // Initializing UI components
        userNameLogIn = findViewById(R.id.username);
        passwordLogIn = findViewById(R.id.password);
        loginbtn = findViewById(R.id.loginbtn);
        googlebtn = findViewById(R.id.googlebtn);
        newaccountbtn = findViewById(R.id.newaccount);

        // Configure Google Sign-In to request the user data required by your app
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .requestScopes(new Scope(CalendarScopes.CALENDAR)) // enable apis
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // User is already signed in. Sign them out.
            signOut(mGoogleSignInClient);
        }

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

                // Authenticate user
                if (db.checkUser(strUserName, strPassword)) {
                    int userId = db.getUserId(strUserName, strPassword);
                    if (userId != -1) {
                        // Successful login
                        Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, ToDoMainActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login error
                        Toast.makeText(LoginActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Invalid credentials
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

    // Sign out method
    private void signOut(GoogleSignInClient mGoogleSignInClient) {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    // Handle sign out (update UI, navigate to login screen, etc.)
                    // Toast.makeText(this, "Successfully signed out", Toast.LENGTH_SHORT).show();
                    // Here you can update your UI or navigate back to your login screen, etc.
                });
    }

    // Initiate sign in
    private void signIn() {
        // Reset the user's sign-in state before attempting to sign in
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Start sign in intent
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from the Google Sign-In intent
        if (requestCode == RC_SIGN_IN) {
            // Get the task from the Google Sign-In intent
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            // Check if the sign-in was successful
            if (resultCode == RESULT_OK && task.isSuccessful()) {
                GoogleSignInAccount account = null;
                try {
                    // Attempt to retrieve the GoogleSignInAccount from the task
                    account = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    // If GoogleSignInAccount cannot be retrieved, throw an exception
                    throw new RuntimeException(e);
                }
                if (account != null) {
                    // If an account was successfully retrieved, get the account ID
                    String googleAccountId = account.getId();
                    // Handle the successful sign-in result
                    handleSignInResult(task, googleAccountId);
                }
            } else {
                // Sign in failed
                Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle sign-in success
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask, String googleAccountId) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully
            if (account != null) {
                Toast.makeText(this, "Successfully signed in as " + account.getEmail(), Toast.LENGTH_SHORT).show();

                // Navigate to main activity
                Intent intent = new Intent(LoginActivity.this, ToDoMainActivity.class);
                intent.putExtra("userId", googleAccountId);
                startActivity(intent);
                finish();
                // createAndAddEventToGoogleCalendar(account);
            }
        } catch (ApiException e) {
            // Handle sign in failure
            Toast.makeText(this, "Google Sign In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // Handle failed connection to Google Play Services
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection to Google Play Services failed", Toast.LENGTH_SHORT).show();
    }

}
