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

    public dbStepCounter(Context context) {
        dbHelper = new dbConnect(context);
    }

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
            db.update(dbConnect.stepsTable, values, selection, selectionArgs);
        } else {
            db.insert(dbConnect.stepsTable, null, values);
        }
        cursor.close();
        db.close();
    }

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

    public Cursor getAllSteps() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + dbConnect.stepsTable, null);
    }

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
            db.update(dbConnect.stepsTable, values, selection, selectionArgs);
        } else {
            db.insert(dbConnect.stepsTable, null, values);
        }
        cursor.close();
        db.close();
    }
}
