package com.example.youdo;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;

import com.example.youdo.AppDatabase;
import com.example.youdo.ToDo;
import com.example.youdo.ToDoDao;
import com.example.youdo.databinding.ActivityMainBinding;

import java.util.List;

public class ToDoRepository extends Activity {
    ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

    private ToDoDao mToDoDao;
    private LiveData<List<ToDo>> mAllToDos;


    public ToDoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mToDoDao = db.toDoDao();
        mAllToDos = mToDoDao.getAllToDos();
    }


    public LiveData<List<ToDo>> getAllToDos() {
        return mAllToDos;
    }


    public void insert(ToDo toDo) {
        new insertAsyncTask(mToDoDao).execute(toDo);
    }

    /**
     * @param hasCapture True if the window has pointer capture.
     */
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private static class insertAsyncTask extends AsyncTask<ToDo, Void, Void> {

        private ToDoDao mAsyncTaskDao;

        insertAsyncTask(ToDoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ToDo... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }


}
