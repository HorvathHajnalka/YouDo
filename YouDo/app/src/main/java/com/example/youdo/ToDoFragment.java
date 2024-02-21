package com.example.youdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import java.util.List;

public class ToDoFragment extends Fragment {

    private ToDoViewModel mToDoViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel inicializálása
        mToDoViewModel = new ViewModelProvider(this).get(ToDoViewModel.class);

        // LiveData figyelése és UI frissítése változás esetén
        mToDoViewModel.getAllToDos().observe(getViewLifecycleOwner(), new Observer<List<ToDo>>() {
            @Override
            public void onChanged(@Nullable final List<ToDo> toDos) {
                // Itt frissítsd a UI-t az új "toDos" listával, például frissítve egy RecyclerView-t
            }
        });
    }
}
