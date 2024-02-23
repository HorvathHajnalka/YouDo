package com.example.youdo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class UploadToDoActivity extends AppCompatActivity {


    EditText uploadName, uploadDesc;
    MaterialButton addNewTypeBtn, saveBtn;

    dbConnectToDo db = new dbConnectToDo(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_do);

        uploadName = findViewById(R.id.addToDoName);
        uploadDesc = findViewById(R.id.addToDoDesc);

        MaterialButton addNewTypeBtn  = (MaterialButton) findViewById(R.id.newtypebtn);
        TextView saveBtn = (TextView) findViewById(R.id.saveTodoBtn);



        addNewTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UploadToDoActivity.this,NewTypeActivity.class));
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strToDoName = uploadName.getText().toString();
                String strToDoDesc = uploadDesc.getText().toString();


                if(strToDoName.isEmpty()) {
                    Toast.makeText(UploadToDoActivity.this, "Empty name!", Toast.LENGTH_SHORT).show();
                } else {
                    ToDo newtodo = new ToDo();
                    newtodo.setName(strToDoName);
                    newtodo.setDescription(strToDoDesc);
                    db.addToDo(newtodo);
                    Toast.makeText(UploadToDoActivity.this, "Successfully added to your ToDo list!!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UploadToDoActivity.this, ToDoMainActivity.class));
                }
            }
        });
    }


}