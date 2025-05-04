package com.example.youdo.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.youdo.Models.Type;

import java.util.ArrayList;
import java.util.List;

// manages database operations for To-Do types.
public class dbConnectToDoType {
    private dbConnect dbHelper;

    public dbConnectToDoType(Context context) {
        dbHelper = new dbConnect(context);
    }

    public Type getToDoTypeById(int typeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                dbConnect.typeTable,
                new String[]{
                        dbConnect.typeId,
                        dbConnect.typeName,
                        dbConnect.typeColour,
                        dbConnect.typeUserId,
                        dbConnect.sumTargetMinutes,
                        dbConnect.sumAchievedMinutes,
                        dbConnect.rewardOverAchievement
                },
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
            type.setSumTargetMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.sumTargetMinutes)));
            type.setSumAchievedMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.sumAchievedMinutes)));
            // SQLite-ben a logikai érték általában int, 0 = false, 1 = true
            int rewardVal = cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.rewardOverAchievement));
            type.setRewardOverAchievement(rewardVal != 0);
            cursor.close();
        }
        db.close();
        return type;
    }

    public int addToDoType(Type type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.typeName, type.getName());
        values.put(dbConnect.typeColour, type.getColour());
        values.put(dbConnect.typeUserId, type.getUserId());
        values.put(dbConnect.sumTargetMinutes, type.getSumTargetMinutes());
        values.put(dbConnect.sumAchievedMinutes, type.getSumAchievedMinutes());
        values.put(dbConnect.rewardOverAchievement, type.isRewardOverAchievement() ? 1 : 0);

        int id = (int) db.insert(dbConnect.typeTable, null, values);
        db.close();
        return id;
    }

    public boolean updateToDoType(Type type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbConnect.typeName, type.getName());
        values.put(dbConnect.typeColour, type.getColour());
        values.put(dbConnect.typeUserId, type.getUserId());
        values.put(dbConnect.sumTargetMinutes, type.getSumTargetMinutes());
        values.put(dbConnect.sumAchievedMinutes, type.getSumAchievedMinutes());
        values.put(dbConnect.rewardOverAchievement, type.isRewardOverAchievement() ? 1 : 0);

        int result = db.update(
                dbConnect.typeTable,
                values,
                dbConnect.typeId + " =?",
                new String[]{String.valueOf(type.getTypeId())});
        db.close();
        return result > 0;
    }

    public boolean deleteToDoTypeById(int typeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(
                dbConnect.typeTable,
                dbConnect.typeId + " =?",
                new String[]{String.valueOf(typeId)});
        db.close();
        return result > 0;
    }

    public List<Type> getAllToDoTypesForUser(int userId) {
        List<Type> typeList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + dbConnect.typeTable + " WHERE " +
                dbConnect.typeUserId + " = ? OR " + dbConnect.typeUserId + " IS NULL";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Type type = new Type();
                type.setTypeId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.typeId)));
                type.setName(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.typeName)));
                type.setColour(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.typeColour)));
                type.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.typeUserId)));
                type.setSumTargetMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.sumTargetMinutes)));
                type.setSumAchievedMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.sumAchievedMinutes)));
                int rewardVal = cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.rewardOverAchievement));
                type.setRewardOverAchievement(rewardVal != 0);
                typeList.add(type);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return typeList;
    }

    public boolean hasToDosAssigned(int typeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + dbConnect.todoTable +
                        " WHERE " + dbConnect.todoTypeId + " = ?",
                new String[]{String.valueOf(typeId)});

        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();
            db.close();
            return count > 0;
        }
        db.close();
        return false;
    }

    public List<Type> getAllUserToDoTypesForUser(int userId) {
        List<Type> typeList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + dbConnect.typeTable + " WHERE " + dbConnect.typeUserId + " = ? ";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Type type = new Type();
                type.setTypeId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.typeId)));
                type.setName(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.typeName)));
                type.setColour(cursor.getString(cursor.getColumnIndexOrThrow(dbConnect.typeColour)));
                type.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.typeUserId)));
                type.setSumTargetMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.sumTargetMinutes)));
                type.setSumAchievedMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.sumAchievedMinutes)));
                int rewardVal = cursor.getInt(cursor.getColumnIndexOrThrow(dbConnect.rewardOverAchievement));
                type.setRewardOverAchievement(rewardVal != 0);
                typeList.add(type);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return typeList;
    }
}
