package com.example.youdo;

import androidx.room.Dao;
import androidx.room.Insert;

import com.example.youdo.Type;

@Dao
public interface TypeDao {
    @Insert
    void insert(Type type);

    // ...
}