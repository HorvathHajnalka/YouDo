package com.example.youdo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ToDoMainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    RecyclerView recyclerView;
    List<ToDo> todoList;
    dbConnectToDo dbConnectToDo;
    int userId = -1; // has to be updated


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_main);

        fab = findViewById(R.id.addToDobtn);
        recyclerView = findViewById(R.id.todoRecyclerView);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId")) {
            userId = intent.getIntExtra("userId", -1); // -1 if we don't know the "userId"
        }

        dbConnectToDo = new dbConnectToDo(this);
        todoList = dbConnectToDo.getAllToDoPerUser(userId);


        ToDoAdapter adapter = new ToDoAdapter(todoList, this);
        recyclerView.setAdapter(adapter);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ToDoMainActivity.this, UploadToDoActivity.class);
                i.putExtra("userId", userId);
                startActivity(i);
            }
        });
    }
}
