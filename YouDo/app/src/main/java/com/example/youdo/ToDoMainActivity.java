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

    FloatingActionButton addTodoBtn;
    FloatingActionButton stepCounterBtn;
    RecyclerView recyclerView;
    List<ToDo> todoList;
    dbConnectToDo dbConnectToDo;
    int userId = -1; // has to be updated


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_main);



        addTodoBtn = findViewById(R.id.addToDobtn);
        stepCounterBtn = findViewById(R.id.stepCounterbtn);
        recyclerView = findViewById(R.id.todoRecyclerView);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId")) {
            userId = intent.getIntExtra("userId", -1); // -1 if we don't know the "userId"
        }


        dbConnectToDo = new dbConnectToDo(this);
        todoList = dbConnectToDo.getAllToDoPerUser(userId);


        ToDoAdapter adapter = new ToDoAdapter(todoList, this);
        recyclerView.setAdapter(adapter);


        addTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ToDoMainActivity.this, UploadToDoActivity.class);
                i.putExtra("userId", userId);
                startActivity(i);
            }
        });


        stepCounterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ToDoMainActivity.this, StepCounterActivity.class);
                i.putExtra("userId", userId);
                startActivity(i);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadToDos();
    }
    public void loadToDos() {
        todoList = dbConnectToDo.getAllToDoPerUser(userId);
        ToDoAdapter adapter = new ToDoAdapter(todoList, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
