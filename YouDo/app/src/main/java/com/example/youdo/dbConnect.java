/*package com.example.youdo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

    public boolean checkEmail(String emailcheck){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = " SELECT * FROM "+ userTable + " WHERE " + email + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{emailcheck});
        if (cursor.moveToFirst()){
            // email has already registered
            return true;
        }else{
            return false;
        }
    }

    public boolean checkName(String namecheck){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = " SELECT * FROM "+ userTable + " WHERE " + userName + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{namecheck});
        if (cursor.moveToFirst()){
            // username has already registered
            return true;
        }else{
            return false;
        }
    }

    public void addUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(userName, user.getUserName());
        values.put(email, user.getEmail());
        values.put(password, user.getPassword());

        db.insert(userTable,null,values);
    }

    public boolean updateUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(userName, user.getUserName());
        values.put(email, user.getEmail());
        values.put(password, user.getPassword());

        int endResult = db.update(userTable, values, userId = " =?", new String[]{String.valueOf(user.getUserId())});
        if (endResult > 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean deleteUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();

        int endResult = db.delete(userTable,userId = " =?", new String[]{String.valueOf(user.getUserId())});
        if (endResult > 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkUser(String userNameLogIn, String passwordLogIn){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = " SELECT * FROM "+ userTable + " WHERE " + userName + " =? AND " + password + " =?";
        Cursor cursor = db.rawQuery(query,new String[]{userNameLogIn, passwordLogIn});
        if (cursor.moveToFirst()){
            return true;
        }else{
            return false;
        }
    }


}*/

package com.example.youdo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class dbConnect extends SQLiteOpenHelper {
    // db parameters
    private static String dbName = "youDo_DB";
    private static int dbVersion = 1;
    public static String userTable = "users";
    public static String userId = "userId";
    public static String userName = "userName";
    public static String email = "email";
    public static String password = "password";

    public dbConnect(@Nullable Context context) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + userTable + "(" + userId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + userName + " TEXT, "+ email + " TEXT, "+ password + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ userTable);
        onCreate(db);
    }
}
