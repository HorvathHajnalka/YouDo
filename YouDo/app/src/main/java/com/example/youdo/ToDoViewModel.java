package com.example.youdo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class ToDoViewModel extends AndroidViewModel {

    private ToDoRepository mRepository;
    private LiveData<List<ToDo>> mAllToDos;


    public ToDoViewModel (Application application) {
        super(application);
        mRepository = new ToDoRepository(application);
        mAllToDos = mRepository.getAllToDos();
    }


    public LiveData<List<ToDo>> getAllToDos() {
        return mAllToDos;
    }


    public void insert(ToDo toDo) {
        mRepository.insert(toDo);
    }


}
