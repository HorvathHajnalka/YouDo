package com.example.youdo;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_table",
        foreignKeys = @ForeignKey(entity = Type.class,
                parentColumns = "typeId",
                childColumns = "typeId",
                onDelete = ForeignKey.CASCADE))

public class ToDo {
    @PrimaryKey(autoGenerate = true)
    private int todoId;
    @NonNull
    private String name;
    private String date;
    private String time;
    private String description;
    private int typeId;
    private String state;
    // functions
}

