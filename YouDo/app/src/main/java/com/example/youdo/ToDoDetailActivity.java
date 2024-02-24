package com.example.youdo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class ToDoDetailActivity extends AppCompatActivity {

    TextView detailTitle, detailDesc;
    MaterialButton editTodoBtn, delTodoBtn, doneTodoBtn;
    dbConnectToDo dbConnectToDo;
    ToDo todo;
    int todoId = -1; // has to be updated

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_detail);

        detailTitle = findViewById(R.id.detailTitle);
        detailDesc = findViewById(R.id.detailDesc);
        editTodoBtn = findViewById(R.id.editTodoBtn);
        delTodoBtn = findViewById(R.id.delTodoBtn);
        doneTodoBtn = findViewById(R.id.doneTodoBtn);



        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("todoId")) {
            todoId = intent.getIntExtra("todoId", -1); // -1 if we don't know the "todoId"
        }

        dbConnectToDo = new dbConnectToDo(this);
        todo = dbConnectToDo.getTodoById(todoId);

        if (todo != null) {

            detailTitle.setText(todo.getName());
            detailDesc.setText(todo.getDescription());

        } else {
            Toast.makeText(this, "ToDo not found", Toast.LENGTH_SHORT).show();
            finish(); // last activity
        }
    }
}