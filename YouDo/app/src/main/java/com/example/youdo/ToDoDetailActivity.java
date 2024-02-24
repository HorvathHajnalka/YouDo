package com.example.youdo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class ToDoDetailActivity extends AppCompatActivity {

    TextView detailTitle, detailDesc;
    MaterialButton editTodoBtn, delTodoBtn, doneTodoBtn;
    dbConnectToDo dbConnectToDo;
    ToDo todo;
    int todoId = -1; // has to be updated
    int userId = -1; // has to be updated

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
        if (intent != null && intent.hasExtra("todoId") && intent.hasExtra("userId")) {
            todoId = intent.getIntExtra("todoId", -1); // -1 if we don't know the "todoId"
            userId = intent.getIntExtra("userId", -1); // -1 if we don't know the "userId"
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

        delTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbConnectToDo.deleteToDoById(todoId);
                Toast.makeText(ToDoDetailActivity.this, "ToDo has been deleted!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(ToDoDetailActivity.this, ToDoMainActivity.class);
                i.putExtra("userId", userId);
                startActivity(i);
            }
        });

    }
}