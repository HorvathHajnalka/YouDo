package com.example.youdo.Activities;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youdo.Database.dbConnectToDo;
import com.example.youdo.Database.dbConnectToDoType;
import com.example.youdo.Models.ToDo;
import com.example.youdo.Models.Type;
import com.example.youdo.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import java.util.Collections;

public class ToDoDetailActivity extends AppCompatActivity {

    TextView detailTitle, detailDesc, detailDate, targetTimeText, achievedTimeText, typeText;
    MaterialButton editTodoBtn, delTodoBtn, doneTodoBtn;
    com.example.youdo.Database.dbConnectToDo dbConnectToDo;
    com.example.youdo.Database.dbConnectToDoType dbConnectToDoType;
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
        targetTimeText = findViewById(R.id.targetTimeText);
        achievedTimeText = findViewById(R.id.achievedTimeText);
        typeText= findViewById(R.id.typeText);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("todoId") && intent.hasExtra("userId")) {
            todoId = intent.getIntExtra("todoId", -1); // -1 if we don't know the "todoId"
            userId = intent.getIntExtra("userId", -1); // -1 if we don't know the "userId"
            curr_date = intent.getStringExtra("curr_date");
        }

        dbConnectToDo = new dbConnectToDo(this);
        dbConnectToDoType = new dbConnectToDoType(this);

        todo = dbConnectToDo.getTodoById(todoId);

        // send UI the to-do details if to-do exists
        if (todo != null) {
            detailTitle.setText(todo.getName());
            if(! todo.getDescription().equals("")) detailDesc.setText(todo.getDescription());
            else {
                detailDesc.setText("");
                detailDesc.setHeight(1);
                detailDesc.setBackgroundColor(ContextCompat.getColor(ToDoDetailActivity.this, R.color.blue_extra_dark));
            }
            detailDate.setText("Date: " + todo.getDate());
            targetTimeText.setText("Target Time: " + todo.getTargetMinutes() + " min.");
            achievedTimeText.setText("Achieved Time: " + todo.getAchievedMinutes() + " min.");
            Type curr_type = dbConnectToDoType.getToDoTypeById(todo.getTypeId());
            if(curr_type!= null){
                typeText.setText("Activity Type: " + curr_type.getName());
            }else{
                typeText.setText("Activity Type: -");
            }
            if (todo.isDone()) doneTodoBtn.setText("ToDo is Not Done!");

        } else {
            Toast.makeText(this, "ToDo not found", Toast.LENGTH_SHORT).show();
            finish(); // Closing the activity if the to-do is not found
        }

        doneTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (todo.getTargetMinutes() > 0 && !todo.isDone()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ToDoDetailActivity.this);
                    builder.setTitle("Completion Time");

                    // EditText
                    final EditText input = new EditText(ToDoDetailActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setHint("Enter achieved time (minutes)");
                    builder.setView(input);

                    // Buttons
                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String enteredTime = input.getText().toString().trim();
                            if (!enteredTime.isEmpty()) {
                                int achievedTime = Integer.parseInt(enteredTime);
                                todo.setAchievedMinutes(achievedTime);
                            } else {
                                todo.setAchievedMinutes(0);
                            }

                            toggleTodoStatus();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                } else {
                    todo.setAchievedMinutes(0);
                    toggleTodoStatus();
                }
            }

            private void toggleTodoStatus() {
                todo.setDone(!todo.isDone());
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
                // Deleting the to-do by its id from the database
                dbConnectToDo.deleteToDoById(todoId);
                Toast.makeText(ToDoDetailActivity.this, "ToDo has been deleted!", Toast.LENGTH_SHORT).show();

                // Attempting to remove the corresponding event from Google Calendar if linked
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
                    // Setting up the Google Calendar API credentials
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

                    // Deleting the event from Google Calendar
                    service.events().delete("primary", eventId).execute();
                } catch (Exception e) {
                    Log.e("deleteEvent", "Error deleting event from Google Calendar", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // Notifying the user about the deletion from Google Calendar
                super.onPostExecute(aVoid);
                Toast.makeText(ToDoDetailActivity.this, "Event deleted from Google Calendar", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}