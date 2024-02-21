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

    public Type(@NonNull String name, String colour) {
        this.name = name;
        this.colour = colour;
    }
    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }
}
