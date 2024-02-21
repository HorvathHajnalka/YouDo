package com.example.youdo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    EditText emailReg, userNameReg, passwordReg, password2Reg;
    dbConnect db = new dbConnect(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        emailReg = findViewById(R.id.email);
        userNameReg = findViewById(R.id.username);
        passwordReg = findViewById(R.id.password);
        password2Reg = findViewById(R.id.password2);

        MaterialButton regbtn  = (MaterialButton) findViewById(R.id.regbtn);
        TextView newaccountbtn  = (TextView) findViewById(R.id.alreadyHaveAccount);


        // switch to login page
        newaccountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });


        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strEmail = emailReg.getText().toString();
                String strUserName = userNameReg.getText().toString();
                String strPassword = passwordReg.getText().toString();
                String strPassword2 = password2Reg.getText().toString();

                if(strUserName.isEmpty() || strEmail.isEmpty() || strPassword.isEmpty() || strPassword2.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Empty fields!", Toast.LENGTH_SHORT).show();
                } else if(db.checkEmail(strEmail)){
                    Toast.makeText(RegisterActivity.this, "Email has already registered!", Toast.LENGTH_SHORT).show();
                } else if (! strPassword.equals(strPassword2)) {
                    Toast.makeText(RegisterActivity.this, "Passwords not matching!", Toast.LENGTH_SHORT).show();
                } else {
                    User newuser = new User(strUserName, strEmail, strPassword);
                    db.addUser(newuser);
                    Toast.makeText(RegisterActivity.this, "Successful Registration!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}