package com.example.youdo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class dbConnect extends SQLiteOpenHelper {
    // db parameters

    // database
    private static String dbName = "youDo_DB";
    private static int dbVersion = 6;


    // users
    public static String userTable = "user";
    public static String userId = "userId";
    public static String userName = "userName";
    public static String email = "email";
    public static String password = "password";
    public static String googleId = "googleId";

    // todos
    public static String todoTable = "todo";
    public static String todoId = "todoId";
    public static String googleTodoId = "googleTodoId";
    public static String todoName = "todoName";
    public static String todoDesc = "todoDesc";
    public static String todoState = "todoState"; // Boolean?
    public static String todoDate = "todoDate"; // Date?
    public static String todoTime = "todoTime"; // Time?
    // ?
    public static String todoTypeId = "todoTypeId"; // foreign key
    public static String todoUserId = "todoUserId"; // foreign key

    // type
    public static String typeTable = "type";
    public static String typeId = "typeId";
    public static String typeName = "typeName";
    public static String typeColour = "typeColour";


    // steps
    public static String stepsTable = "steps";
    public static String stepId = "stepId";
    public static String deviceId = "deviceId"; //
    public static String date = "date"; // yyy-mm-dd
    public static String steps = "steps"; // stepcount on a given dayd

    // devices

    public static String devicesTable = "devices";
    public static String registrationDate = "registrationDate";

    public dbConnect(@Nullable Context context) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // user table
        String makeUserQuery = "CREATE TABLE " + userTable + "(" + userId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + userName + " TEXT, " + email + " TEXT, " + password + " TEXT, " + googleId + " TEXT UNIQUE)";
        db.execSQL(makeUserQuery);

        // to do table
        String makeTodoQuery = "CREATE TABLE " + todoTable + "(" + todoId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + googleTodoId + " TEXT, " + todoName + " TEXT, "+ todoDesc + " TEXT, "+ todoState + " TEXT, " + todoDate + " TEXT, "
                + todoTime + " TEXT, " + todoTypeId + " INTEGER, " + todoUserId + " INTEGER, "
                + "FOREIGN KEY(" + todoTypeId + ") REFERENCES " + typeTable + "(" + typeId + "), "
                + "FOREIGN KEY(" + todoUserId + ") REFERENCES " + userTable + "(" + userId + "))";
        db.execSQL(makeTodoQuery);

        // type table
        String makeTypeQuery = "CREATE TABLE " + typeTable + "(" + typeId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + typeName + " TEXT, "+ typeColour + " TEXT)";
        db.execSQL(makeTypeQuery);

        // steps table
        String makeStepsQuery = "CREATE TABLE " + stepsTable + "(" + stepId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + deviceId + " TEXT, " + date + " TEXT, " + steps + " INTEGER)";
        db.execSQL(makeStepsQuery);

        // devices
        String makeDeviceTableQuery = "CREATE TABLE " + devicesTable + "(" +
                deviceId + " TEXT PRIMARY KEY, " +
                registrationDate + " TEXT)";
        db.execSQL(makeDeviceTableQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ userTable);
        db.execSQL("DROP TABLE IF EXISTS "+ todoTable);
        db.execSQL("DROP TABLE IF EXISTS "+ typeTable);
        db.execSQL("DROP TABLE IF EXISTS "+ stepsTable);
        onCreate(db);
    }
}
