package com.example.youdo;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class dbConnect extends SQLiteOpenHelper {

    private static String userTable = "users";
    private String password = "password";
    private static String userId = "userId";
    private String userName = "userName";
    private String email = "email";
    // db parameters
    private static String dbName = "youDo_DB";
    private static int dbVersion = 1;
    public dbConnect(@Nullable Context context) {
        super(context, dbName, null, dbVersion);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + userTable + "(" + userId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + userName + " TEXT, "+ email + " TEXT, "+password+ " TEXT)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ userTable);
        onCreate(db);
    }

    public void addUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(userName, user.getUserName());
        values.put(email, user.getEmail());
        values.put(password, user.getPassword());

        // db.insert(userTable, null, values);
        db.insert(userTable,null,values);
    }

    public boolean updateUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(userName, user.getUserName());
        values.put(email, user.getEmail());
        values.put(password, user.getPassword());

        int endResult = db.update(userTable, values, userId = "=?", new String[]{String.valueOf(user.getUserId())});
        if (endResult > 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean deleteUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();

        int endResult = db.delete(userTable,userId = "=?", new String[]{String.valueOf(user.getUserId())});
        if (endResult > 0){
            return true;
        }else{
            return false;
        }
    }
}
