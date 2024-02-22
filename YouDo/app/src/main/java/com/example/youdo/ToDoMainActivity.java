package com.example.youdo;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class ToDoMainActivity extends AppCompatActivity {

    private RecyclerView todoRecView;
    private ToDoAdapter todoAdapter;
    private List<ToDo> todoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_main);
        getSupportActionBar().hide();

        todoList = new ArrayList<>();

        todoRecView = findViewById(R.id.todoRecyclerView);
        todoRecView.setLayoutManager(new LinearLayoutManager(this));
        todoAdapter = new ToDoAdapter(this);
        todoRecView.setAdapter(todoAdapter);


        ToDo todo = new ToDo();
        todo.setName("test");
        todo.setState("0");

        todo.setTodoId(1);

        todoList.add(todo);
        todoList.add(todo);
        todoList.add(todo);
        todoList.add(todo);
        todoList.add(todo);
        todoList.add(todo);

        todoAdapter.setTodo(todoList);

    }
}