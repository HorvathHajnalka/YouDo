package com.example.youdo;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "type_table")
public class Type {
    @PrimaryKey(autoGenerate = true)
    private int typeId;
    @NonNull
    private String name;
    private String colour;
    // functions
}
