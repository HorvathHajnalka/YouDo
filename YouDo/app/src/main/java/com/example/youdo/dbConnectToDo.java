package com.example.youdo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class dbConnectToDo {
    private dbConnect dbHelper;

    public dbConnectToDo(Context context) {
        dbHelper = new dbConnect(context);
    }


    public int addToDo(ToDo todo){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.todoName, todo.getName());
        values.put(dbConnect.googleTodoId, todo.getGoogleTodoId());
        values.put(dbConnect.todoDesc, todo.getDescription());
        values.put(dbConnect.todoDone, String.valueOf(todo.isDone()));
        values.put(dbConnect.todoDate, todo.getDate());
        values.put(dbConnect.todoTime, todo.getTime());
        values.put(dbConnect.todoTypeId, todo.getTypeId());
        values.put(dbConnect.todoUserId, todo.getUserId());


        int id = (int) db.insert(dbConnect.todoTable, null, values);
        db.close();

        return id;
    }

    public boolean updateToDo(ToDo todo){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.todoName, todo.getName());
        values.put(dbConnect.googleTodoId, todo.getGoogleTodoId());
        values.put(dbConnect.todoDesc, todo.getDescription());
        values.put(dbConnect.todoDone, String.valueOf(todo.isDone()));
        values.put(dbConnect.todoDate, todo.getDate());
        values.put(dbConnect.todoTime, todo.getTime());
        values.put(dbConnect.todoTypeId, todo.getTypeId());
        values.put(dbConnect.todoUserId, todo.getUserId());

        int endResult = db.update(dbConnect.todoTable, values, dbConnect.todoId + " =?", new String[]{String.valueOf(todo.getTodoId())});
        db.close();
        return endResult > 0;
    }


    public boolean deleteToDoById(int todoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int endResult = db.delete(dbConnect.todoTable, dbConnect.todoId + " =?", new String[]{String.valueOf(todoId)});
        db.close();
        return endResult > 0;
    }

    public boolean getToDoByGoogleId(String googleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {dbConnect.googleTodoId};
        String selection = dbConnect.googleTodoId + " =?";
        String[] selectionArgs = {googleId};

        Cursor cursor = db.query(dbConnect.todoTable, columns, selection, selectionArgs, null, null, null);

        boolean exists = cursor.getCount() > 0;

        cursor.close();

        db.close();

        return exists;
    }



    public List<ToDo> getAllToDoPerUser(int uId) {
        List<ToDo> todoList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                dbConnect.todoId,
                dbConnect.googleTodoId,
                dbConnect.todoName,
                dbConnect.todoDesc,
                dbConnect.todoDate,
                dbConnect.todoTime,
                dbConnect.todoTypeId,
                dbConnect.todoUserId,
                dbConnect.todoDone
        };

        String selection = dbConnect.todoUserId + " =?";
        String[] selectionArgs = {String.valueOf(uId)};

        Cursor cursor = db.query(dbConnect.todoTable,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ToDo todo = new ToDo();
                int todoIdIndex = cursor.getColumnIndex(dbConnect.todoId);
                int googleTodoIdIndex = cursor.getColumnIndex(dbConnect.googleTodoId);
                int nameIndex = cursor.getColumnIndex(dbConnect.todoName);
                int descIndex = cursor.getColumnIndex(dbConnect.todoDesc);
                int dateIndex = cursor.getColumnIndex(dbConnect.todoDate);
                int timeIndex = cursor.getColumnIndex(dbConnect.todoTime);
                int typeIdIndex = cursor.getColumnIndex(dbConnect.todoTypeId);
                int userIdIndex = cursor.getColumnIndex(dbConnect.todoUserId);
                int stateIndex = cursor.getColumnIndex(dbConnect.todoDone);

                if (todoIdIndex != -1) todo.setTodoId(cursor.getInt(todoIdIndex));
                if (googleTodoIdIndex != -1) todo.setGoogleTodoId(cursor.getString(googleTodoIdIndex));
                if (nameIndex != -1) todo.setName(cursor.getString(nameIndex));
                if (descIndex != -1) todo.setDescription(cursor.getString(descIndex));
                if (dateIndex != -1) todo.setDate(cursor.getString(dateIndex));
                if (timeIndex != -1) todo.setTime(cursor.getString(timeIndex));
                if (typeIdIndex != -1) todo.setTypeId(cursor.getInt(typeIdIndex));
                if (userIdIndex != -1) todo.setUserId(cursor.getInt(userIdIndex));
                if (stateIndex != -1 && Objects.equals(cursor.getString(stateIndex), "true")) todo.setDone(true);
                if (stateIndex != -1 && Objects.equals(cursor.getString(stateIndex), "false")) todo.setDone(false);


                todoList.add(todo);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return todoList;
    }


    public ToDo getTodoById(int todoId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = dbConnect.todoId + " =?";
        String[] selectionArgs = {String.valueOf(todoId)};

        Cursor cursor = db.query(dbConnect.todoTable,
                new String[]{dbConnect.todoId,dbConnect.googleTodoId, dbConnect.todoName, dbConnect.todoDesc, dbConnect.todoDone, dbConnect.todoDate, dbConnect.todoTime, dbConnect.todoTypeId, dbConnect.todoUserId},
                selection, selectionArgs, null, null, null);

        ToDo todo = null;
        if (cursor != null && cursor.moveToFirst()) {
            todo = new ToDo();
            int idIndex = cursor.getColumnIndex(dbConnect.todoId);
            int googleIdIndex = cursor.getColumnIndex(dbConnect.googleTodoId);
            int nameIndex = cursor.getColumnIndex(dbConnect.todoName);
            int descIndex = cursor.getColumnIndex(dbConnect.todoDesc);
            int stateIndex = cursor.getColumnIndex(dbConnect.todoDone);
            int dateIndex = cursor.getColumnIndex(dbConnect.todoDate);
            int timeIndex = cursor.getColumnIndex(dbConnect.todoTime);
            int typeIdIndex = cursor.getColumnIndex(dbConnect.todoTypeId);
            int userIdIndex = cursor.getColumnIndex(dbConnect.todoUserId);

            if (idIndex != -1) todo.setTodoId(cursor.getInt(idIndex));
            if (googleIdIndex != -1) todo.setGoogleTodoId(cursor.getString(googleIdIndex));
            if (nameIndex != -1) todo.setName(cursor.getString(nameIndex));
            if (descIndex != -1) todo.setDescription(cursor.getString(descIndex));
            if (stateIndex != -1 && Objects.equals(cursor.getString(stateIndex), "true")) todo.setDone(true);
            if (stateIndex != -1 && Objects.equals(cursor.getString(stateIndex), "false")) todo.setDone(false);
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

    public List<ToDo> getToDosByUserAndDate(int userId, String date) {
        List<ToDo> filteredToDoList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // A kiválasztandó oszlopok
        String[] projection = {
                dbConnect.todoId,
                dbConnect.googleTodoId,
                dbConnect.todoName,
                dbConnect.todoDesc,
                dbConnect.todoDate,
                dbConnect.todoTime,
                dbConnect.todoTypeId,
                dbConnect.todoUserId,
                dbConnect.todoDone
        };

        // A keresési feltételek
        String selection = dbConnect.todoUserId + " = ? AND " + dbConnect.todoDate + " = ?";
        String[] selectionArgs = {String.valueOf(userId), date};

        // A lekérdezés végrehajtása
        Cursor cursor = db.query(
                dbConnect.todoTable,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // Az eredmények feldolgozása
        if (cursor.moveToFirst()) {
            do {
                ToDo todo = new ToDo();
                todo.setTodoId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.todoId)));
                todo.setGoogleTodoId(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.googleTodoId)));
                todo.setName(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.todoName)));
                todo.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.todoDesc)));
                todo.setDate(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.todoDate)));
                todo.setTime(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.todoTime)));
                todo.setTypeId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.todoTypeId)));
                todo.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.todoUserId)));
                todo.setDone("true".equals(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.todoDone))));

                filteredToDoList.add(todo);
            } while (cursor.moveToNext());
        }

        // Erőforrások felszabadítása
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return filteredToDoList;
    }


}