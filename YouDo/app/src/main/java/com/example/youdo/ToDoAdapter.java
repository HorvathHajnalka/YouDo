package com.example.youdo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youdo.ToDo;

import java.util.List;

/*
  Adapter class for managing a list of To-Do items in a RecyclerView.
  Each To-Do item is represented by a button. This adapter is responsible
  for binding To-Do data to views, handling layout inflation, and setting
  up click listeners to navigate to detail views for each To-Do item.
 */

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDo> todoList;
    private Context context;
    String curr_date;
    dbConnectToDoType db;
    public ToDoAdapter(List<ToDo> todoList, Context context,String curr_date) {
        this.todoList = todoList;
        this.context = context;
        this.curr_date = curr_date;
        this.db = new dbConnectToDoType(context);
    }

    // Inflate the layout for each item in the RecyclerView (each To-Do item will use todo_button_layout)
    @NonNull
    @Override
    public ToDoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_button_layout, parent, false);
        Log.e("myLog", "date "+curr_date);
        return new ViewHolder(view);
    }

    // Bind data to the views in the ViewHolder (set up the To-Do item in the list at the given position)
    @Override
    public void onBindViewHolder(@NonNull ToDoAdapter.ViewHolder holder, int position) {
        final ToDo todo = todoList.get(position);
        holder.todoButton.setText(todo.getName());
        if (todo.isDone()) {
            // If the To-Do is marked as done, change the background color of the button to dark grey
            int darkGrey = ContextCompat.getColor(context, R.color.grey_00);
            holder.todoButton.setBackgroundColor(darkGrey);
            holder.todoButton.setPaintFlags(holder.todoButton.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            if(todo != null && todo.getTypeId() != 0) {
                Type type = db.getToDoTypeById(todo.getTypeId());
                holder.todoButton.setBackgroundColor(Color.parseColor(type.getColour()));
            }
            holder.todoButton.setPaintFlags(holder.todoButton.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.todoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ToDoDetailActivity.class);
                intent.putExtra("todoId", todo.getTodoId());
                intent.putExtra("userId", todo.getUserId());
                intent.putExtra("curr_date", curr_date);



                context.startActivity(intent);
            }
        });
    }

    // Return the total number of items in the todoList
    @Override
    public int getItemCount() {
        return todoList.size();
    }

    // Button representing a single To-Do item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button todoButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            todoButton = itemView.findViewById(R.id.todoButton);
        }
    }
}
