package com.example.youdo.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class dbStepCounter {

    private static dbConnect dbHelper;

    // Constructor to initialize the dbHelper with the application context
    public dbStepCounter(Context context) {
        dbHelper = new dbConnect(context);
    }

    // Adds steps to the database for a specific device and date
    public void addSteps(String userId, String date, int steps) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = dbConnect.stepUserId + " = ? AND " + dbConnect.date + " = ?";
        String[] selectionArgs = {userId, date};
        Cursor cursor = db.query(dbConnect.stepsTable, null, selection, selectionArgs, null, null, null);

        ContentValues values = new ContentValues();
        values.put(dbConnect.stepUserId, userId);
        values.put(dbConnect.date, date);
        values.put(dbConnect.steps, steps);

        if (cursor.moveToFirst()) {
            // If a record already exists, update the steps count
            db.update(dbConnect.stepsTable, values, selection, selectionArgs);
        } else {
            // If the record does not exist, create a new one
            db.insert(dbConnect.stepsTable, null, values);
        }
        cursor.close();
        db.close();
    }

    // Retrieves the step count for a given device and date
    @SuppressLint("Range")
    public int getStepsByDate(String userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {dbConnect.steps};
        String selection = dbConnect.stepUserId + " = ? AND " + dbConnect.date + " = ?";
        String[] selectionArgs = {userId, date};

        Cursor cursor = db.query(dbConnect.stepsTable, columns, selection, selectionArgs, null, null, null);

        int totalSteps = 0;
        if (cursor.moveToFirst()) {
            totalSteps = cursor.getInt(cursor.getColumnIndex(dbConnect.steps));
        }
        cursor.close();
        db.close();
        return totalSteps;
    }

    // Retrieves all step records from the database
    public Cursor getAllSteps() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + dbConnect.stepsTable, null);
    }

    // Adds or updates step count for a given device and date
    public void addOrUpdateSteps(String userId, String date, int steps) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = dbConnect.stepUserId + " = ? AND " + dbConnect.date + " = ?";
        String[] selectionArgs = {userId, date};
        Cursor cursor = db.query(dbConnect.stepsTable, null, selection, selectionArgs, null, null, null);

        ContentValues values = new ContentValues();
        values.put(dbConnect.stepUserId, userId);
        values.put(dbConnect.date, date);
        values.put(dbConnect.steps, steps);

        if (cursor.moveToFirst()) {
            // Update the step count if the record already exists
            db.update(dbConnect.stepsTable, values, selection, selectionArgs);
        } else {
            // Create a new record if it doesn't exist
            db.insert(dbConnect.stepsTable, null, values);
        }
        cursor.close();
        db.close();
    }
}
