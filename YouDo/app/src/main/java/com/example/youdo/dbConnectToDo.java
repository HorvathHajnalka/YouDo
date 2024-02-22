package com.example.youdo;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class dbConnectToDo {
    private dbConnect dbHelper;

    public dbConnectToDo(Context context) {
        dbHelper = new dbConnect(context);
    }

    public void addToDo(ToDo todo){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.todoName, todo.getName());
        values.put(dbConnect.todoDesc, todo.getDescription());
        values.put(dbConnect.todoState, todo.getState());
        values.put(dbConnect.todoState, todo.getDate());
        values.put(dbConnect.todoState, todo.getTime());

        db.insert(dbConnect.todoTable, null, values);
    }

    public boolean updateToDo(ToDo todo){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.todoName, todo.getName());
        values.put(dbConnect.todoDesc, todo.getDescription());
        values.put(dbConnect.todoState, todo.getState());
        values.put(dbConnect.todoState, todo.getDate());
        values.put(dbConnect.todoState, todo.getTime());

        int endResult = db.update(dbConnect.todoTable, values, dbConnect.todoId + " =?", new String[]{String.valueOf(todo.getTodoId())});
        return endResult > 0;
    }

    public boolean deleteToDo(ToDo todo){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int endResult = db.delete(dbConnect.todoTable, dbConnect.todoId + " =?", new String[]{String.valueOf(todo.getUserId())});
        return endResult > 0;
    }


}