package com.example.youdo;

import android.content.Context;
import android.content.Intent;
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

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDo> todoList;
    private Context context;
    String curr_date;

    public ToDoAdapter(List<ToDo> todoList, Context context,String curr_date) {
        this.todoList = todoList;
        this.context = context;
        this.curr_date = curr_date;
    }

    @NonNull
    @Override
    public ToDoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_button_layout, parent, false);
        Log.e("myLog", "date "+curr_date);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoAdapter.ViewHolder holder, int position) {
        final ToDo todo = todoList.get(position);
        holder.todoButton.setText(todo.getName());
        if (todo.isDone()) {
            int darkGrey = ContextCompat.getColor(context, R.color.grey_00);
            holder.todoButton.setBackgroundColor(darkGrey);
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

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button todoButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            todoButton = itemView.findViewById(R.id.todoButton);
        }
    }
}
