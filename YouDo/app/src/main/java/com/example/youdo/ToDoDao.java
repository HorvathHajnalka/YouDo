package com.example.youdo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.youdo.ToDo;

import java.util.List;

@Dao
public interface ToDoDao {
    @Insert
    void insert(ToDo todo);

    @Update
    void update(ToDo todo);

    @Delete
    void delete(ToDo todo);

    @Query("SELECT * FROM todo_table WHERE typeId = :typeId")
    LiveData<List<ToDo>> getToDosByType(int typeId);
}