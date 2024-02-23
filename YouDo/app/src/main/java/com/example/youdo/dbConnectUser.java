package com.example.youdo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class dbConnectUser {
    private dbConnect dbHelper;

    public dbConnectUser(Context context) {
        dbHelper = new dbConnect(context);
    }

    public boolean checkEmail(String emailcheck) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + dbConnect.userTable + " WHERE " + dbConnect.email + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{emailcheck});
        boolean hasEmail = cursor.moveToFirst();
        cursor.close();
        return hasEmail;
    }

    public boolean checkName(String namecheck){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + dbConnect.userTable + " WHERE " + dbConnect.userName + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{namecheck});
        boolean hasName = cursor.moveToFirst();
        cursor.close();
        return hasName;
    }

    public void addUser(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.userName, user.getUserName());
        values.put(dbConnect.email, user.getEmail());
        values.put(dbConnect.password, user.getPassword());

        db.insert(dbConnect.userTable, null, values);
    }

    public boolean updateUser(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.userName, user.getUserName());
        values.put(dbConnect.email, user.getEmail());
        values.put(dbConnect.password, user.getPassword());

        int endResult = db.update(dbConnect.userTable, values, dbConnect.userId + " =?", new String[]{String.valueOf(user.getUserId())});
        return endResult > 0;
    }

    public boolean deleteUser(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int endResult = db.delete(dbConnect.userTable, dbConnect.userId + " =?", new String[]{String.valueOf(user.getUserId())});
        return endResult > 0;
    }

    public boolean checkUser(String userNameLogIn, String passwordLogIn){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + dbConnect.userTable + " WHERE " + dbConnect.userName + " =? AND " + dbConnect.password + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{userNameLogIn, passwordLogIn});
        boolean hasUser = cursor.moveToFirst();
        cursor.close();
        return hasUser;
    }

    public int getUserId(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + dbConnect.userId + " FROM " + dbConnect.userTable + " WHERE " + dbConnect.userName + " =? AND " + dbConnect.password + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex(dbConnect.userId));
            cursor.close();
            return userId;
        } else {
            cursor.close();
            return -1; // user not found
        }
    }
}
