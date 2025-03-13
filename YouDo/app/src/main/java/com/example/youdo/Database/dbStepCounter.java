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
    public void addSteps(String deviceId, String date, int steps) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = dbConnect.deviceId + " = ? AND " + dbConnect.date + " = ?";
        String[] selectionArgs = {deviceId, date};
        Cursor cursor = db.query(dbConnect.stepsTable, null, selection, selectionArgs, null, null, null);

        ContentValues values = new ContentValues();
        values.put(dbConnect.deviceId, deviceId);
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
    public int getStepsByDate(String deviceId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {dbConnect.steps};
        String selection = dbConnect.deviceId + " = ? AND " + dbConnect.date + " = ?";
        String[] selectionArgs = {deviceId, date};

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

    // Generates or retrieves the unique device ID
    public static String getDeviceId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(dbConnect.devicesTable, new String[]{"deviceId"}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            // If device ID already exists, return it
            String deviceId = cursor.getString(0);
            cursor.close();
            db.close();
            return deviceId;
        } else {
            cursor.close();
            db.close();

            // Generate and save a new device ID if it doesn't exist
            String deviceId = UUID.randomUUID().toString();
            SQLiteDatabase writeDb = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("deviceId", deviceId);
            values.put("registrationDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            writeDb.insert(dbConnect.devicesTable, null, values);
            writeDb.close();

            return deviceId;
        }
    }

    // Adds or updates step count for a given device and date
    public void addOrUpdateSteps(String deviceId, String date, int steps) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = dbConnect.deviceId + " = ? AND " + dbConnect.date + " = ?";
        String[] selectionArgs = {deviceId, date};
        Cursor cursor = db.query(dbConnect.stepsTable, null, selection, selectionArgs, null, null, null);

        ContentValues values = new ContentValues();
        values.put(dbConnect.deviceId, deviceId);
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
