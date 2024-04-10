package com.example.youdo;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

// Manages database operations for to-do item types
public class dbConnectToDoType{
    private dbConnect dbHelper;

    public dbConnectToDoType(Context context) {
        dbHelper = new dbConnect(context);
    }

    // Adds a new type to the database.
    public void addType(Type type){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.typeName, type.getName());
        values.put(dbConnect.typeColour, type.getColour());

        db.insert(dbConnect.typeTable, null, values);
    }

    //  Updates an existing type in the database.
    public boolean updateType(Type type){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.typeName, type.getName());
        values.put(dbConnect.typeColour, type.getColour());

        int endResult = db.update(dbConnect.typeTable, values, dbConnect.typeId + " =?", new String[]{String.valueOf(type.getTypeId())});
        return endResult > 0;
    }

    // Deletes a type from the database.
    public boolean deleteType(Type type){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int endResult = db.delete(dbConnect.typeTable, dbConnect.typeId + " =?", new String[]{String.valueOf(type.getTypeId())});
        return endResult > 0;
    }


}