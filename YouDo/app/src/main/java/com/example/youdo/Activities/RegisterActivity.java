package com.example.youdo.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youdo.Database.dbConnectUser;
import com.example.youdo.Models.User;
import com.example.youdo.R;
import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    EditText emailReg, userNameReg, passwordReg, password2Reg;
    dbConnectUser db = new dbConnectUser(this);
    MaterialButton regbtn;
    TextView haveaccountbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        emailReg = findViewById(R.id.email);
        userNameReg = findViewById(R.id.username);
        passwordReg = findViewById(R.id.password);
        password2Reg = findViewById(R.id.password2);

        regbtn  = (MaterialButton) findViewById(R.id.regbtn);
        haveaccountbtn  = (TextView) findViewById(R.id.alreadyHaveAccount);


        // switch to login page
        haveaccountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });


        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieving user input from EditText fields
                String strEmail = emailReg.getText().toString();
                String strUserName = userNameReg.getText().toString();
                String strPassword = passwordReg.getText().toString();
                String strPassword2 = password2Reg.getText().toString();

                // Input validation and registration logic
                if(strUserName.isEmpty() || strEmail.isEmpty() || strPassword.isEmpty() || strPassword2.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Empty fields!", Toast.LENGTH_SHORT).show();
                }else if(! Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()){
                    Toast.makeText(RegisterActivity.this, "Invalid Email!", Toast.LENGTH_SHORT).show();
                } else if(db.checkEmail(strEmail)){
                    Toast.makeText(RegisterActivity.this, "Email has already registered!", Toast.LENGTH_SHORT).show();
                } else if(db.checkName(strUserName)){
                    Toast.makeText(RegisterActivity.this, "Username has already registered!", Toast.LENGTH_SHORT).show();
                } else if (! strPassword.equals(strPassword2)) {
                    Toast.makeText(RegisterActivity.this, "Passwords not matching!", Toast.LENGTH_SHORT).show();
                } else {
                    // Creating a new user and adding them to the database
                    User newuser = new User(strUserName, strEmail, strPassword);
                    db.addUser(newuser);
                    Toast.makeText(RegisterActivity.this, "Successful Registration!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));

                }
            }
        });
    }
}