package com.example.youdo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

// Manages database operations for To-Do types.
public class dbConnectToDoType {
    private dbConnect dbHelper;

    public dbConnectToDoType(Context context) {
        dbHelper = new dbConnect(context);
    }

    // Retrieves a single To-Do type by its ID.
    public Type getToDoTypeById(int typeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(dbConnect.typeTable,
                new String[]{dbConnect.typeId, dbConnect.typeName, dbConnect.typeColour, dbConnect.typeUserId},
                dbConnect.typeId + " =?",
                new String[]{String.valueOf(typeId)},
                null, null, null);

        Type type = null;
        if (cursor != null && cursor.moveToFirst()) {
            type = new Type();
            type.setTypeId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.typeId)));
            type.setName(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.typeName)));
            type.setColour(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.typeColour)));
            type.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.typeUserId)));
            cursor.close();
        }
        db.close();
        return type;
    }

    // Adds a new To-Do type to the database.
    public int addToDoType(Type type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.typeName, type.getName());
        values.put(dbConnect.typeColour, type.getColour());
        values.put(dbConnect.typeUserId, type.getUserId());

        int id = (int) db.insert(dbConnect.typeTable, null, values);
        db.close();
        return id;
    }

    // Updates an existing To-Do type in the database.
    public boolean updateToDoType(Type type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.typeName, type.getName());
        values.put(dbConnect.typeColour, type.getColour());
        values.put(dbConnect.typeUserId, type.getUserId());

        int result = db.update(dbConnect.typeTable, values, dbConnect.typeId + " =?", new String[]{String.valueOf(type.getTypeId())});
        db.close();
        return result > 0;
    }

    // Deletes a To-Do type from the database by its ID.
    public boolean deleteToDoTypeById(int typeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(dbConnect.typeTable, dbConnect.typeId + " =?", new String[]{String.valueOf(typeId)});
        db.close();
        return result > 0;
    }

    // Retrieves all To-Do types for a specific user (including general types where userId is NULL).
    public List<Type> getAllToDoTypesForUser(int userId) {
        List<Type> typeList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + dbConnect.typeTable + " WHERE " + dbConnect.typeUserId + " = ? OR " + dbConnect.typeUserId + " IS NULL";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Type type = new Type();
                type.setTypeId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.typeId)));
                type.setName(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.typeName)));
                type.setColour(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.typeColour)));
                int userIdIndex = cursor.getColumnIndex(dbConnect.typeUserId);
                if (userIdIndex != -1 && !cursor.isNull(userIdIndex)) {
                    type.setUserId(cursor.getInt(userIdIndex));
                }
                typeList.add(type);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return typeList;
    }
}
