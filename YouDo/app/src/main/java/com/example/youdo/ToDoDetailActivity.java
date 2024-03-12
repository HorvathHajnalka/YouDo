package com.example.youdo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.io.IOException;
import java.util.Collections;

public class ToDoDetailActivity extends AppCompatActivity {

    TextView detailTitle, detailDesc, detailDate;
    MaterialButton editTodoBtn, delTodoBtn, doneTodoBtn;
    dbConnectToDo dbConnectToDo;
    ToDo todo;
    int todoId = -1; // has to be updated
    int userId = -1; // has to be updated
    String curr_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_detail);

        detailTitle = findViewById(R.id.detailTitle);
        detailDesc = findViewById(R.id.detailDesc);
        editTodoBtn = findViewById(R.id.editTodoBtn);
        delTodoBtn = findViewById(R.id.delTodoBtn);
        doneTodoBtn = findViewById(R.id.doneTodoBtn);
        detailDate = findViewById(R.id.detailDate);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("todoId") && intent.hasExtra("userId")) {
            todoId = intent.getIntExtra("todoId", -1); // -1 if we don't know the "todoId"
            userId = intent.getIntExtra("userId", -1); // -1 if we don't know the "userId"
            curr_date = intent.getStringExtra("curr_date");
        }

        dbConnectToDo = new dbConnectToDo(this);
        todo = dbConnectToDo.getTodoById(todoId);

        if (todo != null) {
            detailTitle.setText(todo.getName());
            if(! todo.getDescription().equals("")) detailDesc.setText(todo.getDescription());
            else {
                detailDesc.setText("");
                detailDesc.setHeight(1);
                detailDesc.setBackgroundColor(ContextCompat.getColor(ToDoDetailActivity.this, R.color.blue_extra_dark));
            }
            detailDate.setText("Date: " + todo.getDate());
            if (todo.isDone()) doneTodoBtn.setText("ToDo is Not Done!");

        } else {
            Toast.makeText(this, "ToDo not found", Toast.LENGTH_SHORT).show();
            finish(); // last activity
        }

        doneTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!todo.isDone()) todo.setDone(true);
                else todo.setDone(false);


                dbConnectToDo.updateToDo(todo);

                Intent i = new Intent(ToDoDetailActivity.this, ToDoMainActivity.class);
                i.putExtra("userId", userId);
                i.putExtra("curr_date", curr_date);
                startActivity(i);

            }
        });

        editTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ToDoDetailActivity.this, UploadToDoActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("todoId", todoId);
                intent.putExtra("curr_date", curr_date);
                startActivity(intent);
            }
        });

        delTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbConnectToDo.deleteToDoById(todoId);
                Toast.makeText(ToDoDetailActivity.this, "ToDo has been deleted!", Toast.LENGTH_SHORT).show();

                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(ToDoDetailActivity.this);

                String googleEventId = todo.getGoogleTodoId();
                // Toast.makeText(ToDoDetailActivity.this, "ToDoId = "+googleEventId, Toast.LENGTH_SHORT).show();

                if(account != null && googleEventId != null) {
                    deleteEventFromGoogleCalendar(googleEventId, account);
                }


                Intent i = new Intent(ToDoDetailActivity.this, ToDoMainActivity.class);
                i.putExtra("userId", userId);
                i.putExtra("curr_date", curr_date);
                startActivity(i);
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    private void deleteEventFromGoogleCalendar(String eventId, GoogleSignInAccount account) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                            ToDoDetailActivity.this, Collections.singleton(CalendarScopes.CALENDAR));

                    credential.setSelectedAccountName(account.getAccount().name);

                    // Build the Calendar service
                    com.google.api.services.calendar.Calendar service = null;
                    HttpTransport httpTransport = new NetHttpTransport();
                    service = new com.google.api.services.calendar.Calendar.Builder(
                            httpTransport, new GsonFactory(), credential)
                            .setApplicationName("YouDo")
                            .build();

                    // delete event
                    service.events().delete("primary", eventId).execute();
                } catch (Exception e) {
                    Log.e("deleteEvent", "Error deleting event from Google Calendar", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(ToDoDetailActivity.this, "Event deleted from Google Calendar", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}