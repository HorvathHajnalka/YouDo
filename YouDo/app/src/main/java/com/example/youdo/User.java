package com.example.youdo;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int userId;
    @NonNull
    private String userName;
    @NonNull
    private String email;
    @NonNull
    private String password;

    // functions
}