package com.example.youdo.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

// building the database
public class dbConnect extends SQLiteOpenHelper {
    // db parameters

    // database
    private static String dbName = "youDo_DB";
    private static int dbVersion = 22;


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
    public static String todoDone = "todoDone";
    public static String todoDate = "todoDate";
    public static String todoTargetMinutes = "todoTargetMinutes";
    public static String todoAchievedMinutes = "todoAchievedMinutes";
    public static String todoTypeId = "todoTypeId"; // foreign key
    public static String todoUserId = "todoUserId"; // foreign key

    // type
    public static String typeTable = "type";
    public static String typeId = "typeId";
    public static String typeName = "typeName";
    public static String typeColour = "typeColour";
    public static String typeUserId = "typeUserId"; // foreign key
    public static String sumTargetMinutes = "sumTargetMinutes";
    public static String sumAchievedMinutes = "sumAchievedMinutes";
    public static String rewardOverAchievement = "rewardOverAchievement";


    // steps
    public static String stepsTable = "steps";
    public static String stepId = "stepId";

    public static String date = "date"; // yyyy-mm-dd
    public static String steps = "steps"; // stepcount on a given days
    public static String stepUserId = "stepUserId";

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
                + googleTodoId + " TEXT, " + todoName + " TEXT, "+ todoDesc + " TEXT, "+ todoDone + " TEXT, " + todoDate + " TEXT, "
                + todoTargetMinutes + " INTEGER, " + todoAchievedMinutes + " INTEGER, " + todoTypeId + " INTEGER, " + todoUserId + " INTEGER, "
                + "FOREIGN KEY(" + todoTypeId + ") REFERENCES " + typeTable + "(" + typeId + "), "
                + "FOREIGN KEY(" + todoUserId + ") REFERENCES " + userTable + "(" + userId + "))";
        db.execSQL(makeTodoQuery);

        // type table
        String makeTypeQuery = "CREATE TABLE " + typeTable + "("
                + typeId + " INTEGER PRIMARY KEY AUTOINCREMENT, " + typeName + " TEXT, " + typeColour + " TEXT, "
                + sumTargetMinutes + " INTEGER DEFAULT 0, " + sumAchievedMinutes + " INTEGER DEFAULT 0, "
                + rewardOverAchievement + " BOOLEAN, " + typeUserId + " INTEGER, "
                + "FOREIGN KEY(" + typeUserId + ") REFERENCES " + userTable + "(" + userId + "))";
        db.execSQL(makeTypeQuery);


        // steps table
        String makeStepsQuery = "CREATE TABLE " + stepsTable + "(" + stepId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + date + " TEXT, " + steps + " INTEGER, " + stepUserId + " INTEGER, "+ "FOREIGN KEY(" + stepUserId + ") REFERENCES " + userTable + "(" + userId + "))";
        db.execSQL(makeStepsQuery);

        insertDefaultTypes(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ userTable);
        db.execSQL("DROP TABLE IF EXISTS "+ todoTable);
        db.execSQL("DROP TABLE IF EXISTS "+ typeTable);
        db.execSQL("DROP TABLE IF EXISTS "+ stepsTable);
        onCreate(db);
    }

    private void insertDefaultTypes(SQLiteDatabase db) {
        String insertQuery = "INSERT INTO " + typeTable + " ("
                + typeName + ", "
                + typeColour + ", "
                + sumTargetMinutes + ", "
                + sumAchievedMinutes + ", "
                + rewardOverAchievement + ") VALUES "
                + "('-', '#5849ff', 0, 0, 1), "
                + "('Sport', '#02C8EA', 0, 0, 1), "
                + "('Study', '#FF0065', 0, 0, 1), "
                + "('Self-Improvement', '#B900FF', 0, 0, 1), "
                + "('Work', '#0015FF', 0, 0, 1), "
                + "('Hobby', '#47D701', 0, 0, 1);";

        db.execSQL(insertQuery);
    }

}
