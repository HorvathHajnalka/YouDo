package com.example.youdo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
        values.put(dbConnect.todoDate, todo.getDate());
        values.put(dbConnect.todoTime, todo.getTime());
        values.put(dbConnect.todoTypeId, todo.getTypeId());
        values.put(dbConnect.todoUserId, todo.getUserId());

        db.insert(dbConnect.todoTable, null, values);
        db.close();
    }

    public boolean updateToDo(ToDo todo){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.todoName, todo.getName());
        values.put(dbConnect.todoDesc, todo.getDescription());
        values.put(dbConnect.todoState, todo.getState());
        values.put(dbConnect.todoDate, todo.getDate());
        values.put(dbConnect.todoTime, todo.getTime());
        values.put(dbConnect.todoTypeId, todo.getTypeId());
        values.put(dbConnect.todoUserId, todo.getUserId());

        int endResult = db.update(dbConnect.todoTable, values, dbConnect.todoId + " =?", new String[]{String.valueOf(todo.getTodoId())});
        db.close();
        return endResult > 0;
    }


    public boolean deleteToDo(ToDo todo){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int endResult = db.delete(dbConnect.todoTable, dbConnect.todoId + " =?", new String[]{String.valueOf(todo.getTodoId())}); // Javítva
        db.close();
        return endResult > 0;
    }


    public List<ToDo> getAllToDoPerUser(int uId) {
        List<ToDo> todoList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = dbConnect.todoUserId + " =?";
        String[] selectionArgs = {String.valueOf(uId)};

        Cursor cursor = db.query(dbConnect.todoTable,
                new String[]{dbConnect.todoId, dbConnect.todoName, dbConnect.todoDesc},
                selection, selectionArgs, null, null, null);



        if (cursor != null && cursor.moveToFirst()) {
            do {
                ToDo todo = new ToDo();
                int todoIdIndex = cursor.getColumnIndex(dbConnect.todoId);
                int nameIndex = cursor.getColumnIndex(dbConnect.todoName);
                int descIndex = cursor.getColumnIndex(dbConnect.todoDesc);

                if (todoIdIndex != -1) todo.setTodoId(cursor.getInt(todoIdIndex));
                if (nameIndex != -1) todo.setName(cursor.getString(nameIndex));
                if (descIndex != -1) todo.setDescription(cursor.getString(descIndex));

                todoList.add(todo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return todoList;
    }

    public ToDo getTodoById(int todoId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = dbConnect.todoId + " =?";
        String[] selectionArgs = {String.valueOf(todoId)};

        Cursor cursor = db.query(dbConnect.todoTable,
                new String[]{dbConnect.todoId, dbConnect.todoName, dbConnect.todoDesc, dbConnect.todoState, dbConnect.todoDate, dbConnect.todoTime, dbConnect.todoTypeId, dbConnect.todoUserId},
                selection, selectionArgs, null, null, null);

        ToDo todo = null;
        if (cursor != null && cursor.moveToFirst()) {
            todo = new ToDo();
            int idIndex = cursor.getColumnIndex(dbConnect.todoId);
            int nameIndex = cursor.getColumnIndex(dbConnect.todoName);
            int descIndex = cursor.getColumnIndex(dbConnect.todoDesc);
            int stateIndex = cursor.getColumnIndex(dbConnect.todoState);
            int dateIndex = cursor.getColumnIndex(dbConnect.todoDate);
            int timeIndex = cursor.getColumnIndex(dbConnect.todoTime);
            int typeIdIndex = cursor.getColumnIndex(dbConnect.todoTypeId);
            int userIdIndex = cursor.getColumnIndex(dbConnect.todoUserId);

            if (idIndex != -1) todo.setTodoId(cursor.getInt(idIndex));
            if (nameIndex != -1) todo.setName(cursor.getString(nameIndex));
            if (descIndex != -1) todo.setDescription(cursor.getString(descIndex));
            if (stateIndex != -1) todo.setState(cursor.getString(stateIndex));
            if (dateIndex != -1) todo.setDate(cursor.getString(dateIndex));
            if (timeIndex != -1) todo.setTime(cursor.getString(timeIndex));
            if (typeIdIndex != -1) todo.setTypeId(cursor.getInt(typeIdIndex));
            if (userIdIndex != -1) todo.setUserId(cursor.getInt(userIdIndex));
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return todo;
    }

}