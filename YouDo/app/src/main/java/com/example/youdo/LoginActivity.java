package com.example.youdo;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    EditText userNameLogIn, passwordLogIn;
    dbConnect db = new dbConnect(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameLogIn = findViewById(R.id.username);
        passwordLogIn = findViewById(R.id.password);

        MaterialButton loginbtn  = (MaterialButton) findViewById(R.id.loginbtn);
        TextView newaccountbtn  = (TextView) findViewById(R.id.newaccount);

        // switch to registerpage
        newaccountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        // admin and admin

        loginbtn.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strUserName = userNameLogIn.getText().toString();
                String strPassword = passwordLogIn.getText().toString();



                if(db.checkUser(strUserName, strPassword)) {
                    // correct
                    Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,ToDoMainActivity.class));
                }else{
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        }));

    }
}

