package com.example.youdo;

import static android.app.DownloadManager.COLUMN_ID;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Handles database operations for user
public class dbConnectUser {
    private dbConnect dbHelper;

    public dbConnectUser(Context context) {
        dbHelper = new dbConnect(context);
    }

    String COLUMN_ID = dbConnect.userId;
    String TABLE_USERS = dbConnect.userTable;
    String COLUMN_GOOGLE_ACCOUNT_ID = dbConnect.googleId;


    // Checks if the provided email exists in the database.
    public boolean checkEmail(String emailcheck) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + dbConnect.userTable + " WHERE " + dbConnect.email + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{emailcheck});
        boolean hasEmail = cursor.moveToFirst();
        cursor.close();
        return hasEmail;
    }


    // Checks if the provided username exists in the database.
    public boolean checkName(String namecheck){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + dbConnect.userTable + " WHERE " + dbConnect.userName + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{namecheck});
        boolean hasName = cursor.moveToFirst();
        cursor.close();
        return hasName;
    }

    // Adds a new user to the database.
    public void addUser(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.userName, user.getUserName());
        values.put(dbConnect.email, user.getEmail());
        values.put(dbConnect.password, user.getPassword());
        values.put(dbConnect.googleId, user.getGoogleId());

        db.insert(dbConnect.userTable, null, values);
    }

    // Updates an existing user in the database.
    public boolean updateUser(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.userName, user.getUserName());
        values.put(dbConnect.email, user.getEmail());
        values.put(dbConnect.password, user.getPassword());

        int endResult = db.update(dbConnect.userTable, values, dbConnect.userId + " =?", new String[]{String.valueOf(user.getUserId())});
        return endResult > 0;
    }

    // Deletes a user from the database.
    public boolean deleteUser(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int endResult = db.delete(dbConnect.userTable, dbConnect.userId + " =?", new String[]{String.valueOf(user.getUserId())});
        return endResult > 0;
    }

    // Checks if a user exists with the provided username and password.
    public boolean checkUser(String userNameLogIn, String passwordLogIn){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + dbConnect.userTable + " WHERE " + dbConnect.userName + " =? AND " + dbConnect.password + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{userNameLogIn, passwordLogIn});
        boolean hasUser = cursor.moveToFirst();
        cursor.close();
        return hasUser;
    }

    // Retrieves the user ID for a given username and password.

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

    //  Retrieves the user ID for a given Google account ID.

    @SuppressLint("Range")
    public int getUserIdByGoogleAccountId(String googleAccountId) {
        int userId = -1;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_GOOGLE_ACCOUNT_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{googleAccountId});

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        }

        cursor.close();
        db.close();

        return userId;
    }



}
