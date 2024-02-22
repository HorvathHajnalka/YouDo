package com.example.youdo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter {
    private List<ToDo> toDoList;
    private ToDoMainActivity activity;
    public ToDoAdapter(ToDoMainActivity activity){
        this.activity = activity;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.todo_layout, parent, false);
        return new ViewHolder(itemView);
    }

    /**
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    public void onBindViewHolder(ViewHolder holder, int position){
        ToDo item = toDoList.get(position);
        holder.task.setText(item.getName());
        holder.task.setChecked(toBoolean(item.getState()));
    }

    public int getItemCount(){
        return toDoList.size();
    }

    private boolean toBoolean(String strVal){
        return strVal!="0";
    }

    public void setTodo(List<ToDo> todoList){
        this.toDoList = todoList;
        notifyDataSetChanged();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox task;
        ViewHolder(View view){
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
        }
    }
}
