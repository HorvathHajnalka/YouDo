package com.example.youdo;

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

    private dbConnect dbHelper;

    public dbStepCounter(Context context) {
        dbHelper = new dbConnect(context);
    }

    // Lépések hozzáadása az adatbázishoz
    public void addSteps(String deviceId, String date, int steps) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(dbConnect.deviceId, deviceId);
        values.put(dbConnect.date, date);
        values.put(dbConnect.steps, steps);

        db.insert(dbConnect.stepsTable, null, values);
        db.close();
    }

    // Lekéri az adott nap lépésszámát
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

    // Az összes lépés lekérése
    public Cursor getAllSteps() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + dbConnect.stepsTable, null);
    }

    public String getDeviceId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(dbConnect.devicesTable, new String[]{"deviceId"}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            String deviceId = cursor.getString(0);
            cursor.close();
            db.close();
            return deviceId;
        } else {
            cursor.close();
            db.close();

            // Eszköz ID generálása és mentése
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

}
