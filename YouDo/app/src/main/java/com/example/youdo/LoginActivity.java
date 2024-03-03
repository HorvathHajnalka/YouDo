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

        userNameLogIn = findViewById(R.id.username);
        passwordLogIn = findViewById(R.id.password);

        loginbtn = findViewById(R.id.loginbtn);
        googlebtn = findViewById(R.id.googlebtn);
        newaccountbtn = findViewById(R.id.newaccount);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .requestScopes(new Scope(CalendarScopes.CALENDAR)) // enable apis
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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

    private void signOut(GoogleSignInClient mGoogleSignInClient) {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    // Handle sign out (update UI, navigate to login screen, etc.)
                    // Toast.makeText(this, "Successfully signed out", Toast.LENGTH_SHORT).show();
                    // Here you can update your UI or navigate back to your login screen, etc.
                });
    }

    private void signIn() {
        // Megszakítja a felhasználó aktuális bejelentkezési állapotát
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Ezután indítja el a bejelentkezési szándékot
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
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
                // fail
                Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask, String googleAccountId) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                Toast.makeText(this, "Successfully signed in as " + account.getEmail(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, ToDoMainActivity.class);
                intent.putExtra("userId", googleAccountId);
                startActivity(intent);
                finish();
                // createAndAddEventToGoogleCalendar(account);
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

}
