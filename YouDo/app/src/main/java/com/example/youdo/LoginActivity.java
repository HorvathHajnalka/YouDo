package com.example.youdo;


import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;


public class LoginActivity extends AppCompatActivity {
    TextView newaccountbtn;
    EditText userNameLogIn, passwordLogIn;
    MaterialButton loginbtn, googlebtn;
    dbConnectUser db = new dbConnectUser(this);

    private static final int RC_SIGN_IN = 9001; // code for Google Sign-In authentications
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameLogIn = findViewById(R.id.username);
        passwordLogIn = findViewById(R.id.password);

        loginbtn = (MaterialButton) findViewById(R.id.loginbtn);
        googlebtn = (MaterialButton) findViewById(R.id.googlebtn);
        newaccountbtn = (TextView) findViewById(R.id.newaccount);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // switch to registerpage
        newaccountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // admin and admin

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
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // successful login
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {

            int userId = db.getUserIdByGoogleAccountId(account.getId());

            if (userId == -1) {

                User newUser = new User();
                newUser.setUserName(account.getDisplayName());
                newUser.setEmail(account.getEmail());
                // Google Account ID
                newUser.setGoogleId(account.getId());
                db.addUser(newUser);

                userId = db.getUserIdByGoogleAccountId(account.getId());
            }

            if (userId != -1) {
                Toast.makeText(this, "Signed in with your Google account.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, ToDoMainActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Error with Google sign in", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "Google sign in failed.", Toast.LENGTH_SHORT).show();
        }
    }

}



