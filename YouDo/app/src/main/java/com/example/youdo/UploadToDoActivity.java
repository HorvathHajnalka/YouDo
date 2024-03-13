package com.example.youdo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;


public class UploadToDoActivity extends AppCompatActivity {


    EditText uploadName, uploadDesc;
    Button datePickerBtn, typePickerBtn;
    MaterialButton addNewTypeBtn, saveBtn;
    private DatePickerDialog datePickerDialog;
    TextView mainTitle;

    String strUserId;
    String curr_date;
    int userId;
    int editTodoId;
    Bundle extras;
    dbConnectToDo db = new dbConnectToDo(this);
    GoogleCalendarService googleCalendarService;
    ToDo editTodo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_do);

        initDatePicker();

        uploadName = findViewById(R.id.addToDoName);
        uploadDesc = findViewById(R.id.addToDoDesc);
        mainTitle = findViewById(R.id.mainTitle);
        // addNewTypeBtn = findViewById(R.id.newtypebtn);
        saveBtn = findViewById(R.id.saveTodoBtn);
        datePickerBtn = findViewById(R.id.datePickerBtn);
        // typePickerBtn = findViewById(R.id.typePickerBtn);


        extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("userId", -1);
            strUserId = String.valueOf(userId);
            editTodoId = extras.getInt("todoId", -1);
            curr_date = extras.getString("curr_date", "-1");
        }

        if (editTodoId != -1) {
            editTodo = db.getTodoById(editTodoId);
            mainTitle.setText("Edit ToDo");
            uploadName.setText(editTodo.getName());
            uploadDesc.setText(editTodo.getDescription());

            // convert from yyyy-mm-dd to yyyy/mm/dd

            String originalDateString = editTodo.getDate();
            String formattedDateString = "";

            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy/MM/dd  ");

            try {
                Date date = originalFormat.parse(originalDateString);
                formattedDateString = targetFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            datePickerBtn.setText(formattedDateString);

        } else {
            datePickerBtn.setText(getTodaysDate());
        }

        datePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        /*
        addNewTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UploadToDoActivity.this, NewTypeActivity.class));
            }
        });*/

        /*
        typePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String strToDoName = uploadName.getText().toString();
                String strToDoDesc = uploadDesc.getText().toString();
                Calendar startTime = getStartTime();
                String strDate = getFormattedDate(startTime);

                if (strToDoName.isEmpty()) {
                    Toast.makeText(UploadToDoActivity.this, "Empty name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(UploadToDoActivity.this);

                if (editTodoId == -1) {
                    createNewToDo(strToDoName, strToDoDesc, strDate, account);
                } else {
                    updateExistingToDo(strToDoName, strToDoDesc, strDate, account);
                }

                navigateToMainToDoActivity();
            }
        });

    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        month = month + 1;

        return makeDateString(day, month, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = makeDateString(dayOfMonth, month, year);
                datePickerBtn.setText(date);

            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, R.style.DatePickerDialogTheme, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());


    }

    private String makeDateString(int dayOfMonth, int month, int year) {

        return year + "/" + getMonthFormat(month) + "/" + dayOfMonth + "  ";
    }

    private String getMonthFormat(int month) {
        if (month == 1) return "Jan";
        else if (month == 2) return "Feb";
        else if (month == 3) return "Mar";
        else if (month == 4) return "Apr";
        else if (month == 5) return "May";
        else if (month == 6) return "Jun";
        else if (month == 7) return "Jul";
        else if (month == 8) return "Aug";
        else if (month == 9) return "Sep";
        else if (month == 10) return "Oct";
        else if (month == 11) return "Nov";
        else return "Dec";
    }
    private Calendar getStartTime() {
        DatePicker datePicker = datePickerDialog.getDatePicker();
        Calendar startTime = Calendar.getInstance();
        startTime.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0);
        return startTime;
    }

    private String getFormattedDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime());
    }

    private void createNewToDo(String name, String desc, String date, GoogleSignInAccount account) {
        ToDo newToDo = new ToDo();
        newToDo.setName(name);
        newToDo.setDescription(desc);
        newToDo.setUserId(userId);
        newToDo.setDate(date);
        newToDo.setDone(false);

        int todoId = db.addToDo(newToDo);
        newToDo.setTodoId(todoId);
        Toast.makeText(UploadToDoActivity.this, "Successfully added to your ToDo list!", Toast.LENGTH_SHORT).show();

        handleGoogleCalendarEvent(account, newToDo, true);
    }

    private void updateExistingToDo(String name, String desc, String date, GoogleSignInAccount account) {
        editTodo.setName(name);
        editTodo.setDescription(desc);
        editTodo.setDate(date);

        boolean updated = db.updateToDo(editTodo);
        if (updated) {
            Toast.makeText(UploadToDoActivity.this, "ToDo has been updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(UploadToDoActivity.this, "ToDo update failed!", Toast.LENGTH_SHORT).show();
        }

        handleGoogleCalendarEvent(account, editTodo, false);
    }

    private void handleGoogleCalendarEvent(GoogleSignInAccount account, ToDo todo, boolean isNew) {
        if (account == null) return;


        googleCalendarService = new GoogleCalendarService(UploadToDoActivity.this, account);
        if (isNew) {
            // Create and add new event
            googleCalendarService.createAndAddEventToGoogleCalendar( todo,account, todo.getName(), todo.getDescription(), todo.getDate(),  new EventCallback() {
                @Override
                public void onEventAdded(String eventId) {
                    todo.setGoogleTodoId(eventId);
                    db.updateToDo(todo);
                    Log.d("GoogleCalendarEvent", "Event added successfully. ID: " + eventId);
                }

                @Override
                public void onError() {
                    Log.e("GoogleCalendarEvent", "Failed to add event to Google Calendar");
                }
            });
        } else {
            // Update existing event
            CompletableFuture.runAsync(() -> {
                try {
                    googleCalendarService.updateEvent(account, todo.getGoogleTodoId(), todo.getName(), todo.getDescription(), todo.getDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).thenRun(() -> {

            });
            Toast.makeText(UploadToDoActivity.this, "ToDo has been updated in Google Calendar!", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMainToDoActivity() {
        Intent intent = new Intent(UploadToDoActivity.this, ToDoMainActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("curr_date", curr_date);
        startActivity(intent);
    }
}


