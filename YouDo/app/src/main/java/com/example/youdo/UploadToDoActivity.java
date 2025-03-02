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
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import com.google.android.gms.common.api.Scope;


public class UploadToDoActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    GoogleSignInClient googleSignInClient;
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


        // Get extras passed through the intent that started this activity.
        extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("userId", -1);
            strUserId = String.valueOf(userId);
            editTodoId = extras.getInt("todoId", -1);
            curr_date = extras.getString("curr_date", "-1");
        }

        // Check if we're editing an existing To-Do
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

                // Checking if the user is signed in with Google
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(UploadToDoActivity.this);
                googleSignInClient = GoogleSignIn.getClient(UploadToDoActivity.this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

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

        // Month in Java's Calendar class is zero-based so it needs to be incremented by one to get the correct display value.
        month = month + 1;

        return makeDateString(day, month, year);
    }

    // Initialize the DatePicker dialog.
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Month in Java's Calendar class is zero-based so it needs to be incremented by one for display.
                month = month + 1;
                String date = makeDateString(dayOfMonth, month, year);
                datePickerBtn.setText(date);

            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // Creating the DatePickerDialog instance with the current date as the default selection.
        datePickerDialog = new DatePickerDialog(this, R.style.DatePickerDialogTheme, dateSetListener, year, month, day);
        // Setting the minimum date to the current date, preventing selection of past dates.
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());


    }

    // This method formats the date string by converting the month integer to a string abbreviation and appending it with the day and year.
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

    // Retrieves the selected date from the DatePicker and sets it as the start time in a Calendar instance.
    private Calendar getStartTime() {
        DatePicker datePicker = datePickerDialog.getDatePicker();
        Calendar startTime = Calendar.getInstance();
        startTime.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0);
        return startTime;
    }

    // Formats a Calendar instance to a string using the specified date format.
    private String getFormattedDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime());
    }

    // Creates a new To-Do item with the provided details and adds it to the database. Optionally, it can integrate with Google Calendar.
    private void createNewToDo(String name, String desc, String date, GoogleSignInAccount account) {
        ToDo newToDo = new ToDo();
        newToDo.setName(name);
        newToDo.setDescription(desc);
        newToDo.setUserId(userId);
        newToDo.setDate(date);
        newToDo.setDone(false);

        // Adding the new To-Do to the database and retrieving its unique ID
        int todoId = db.addToDo(newToDo);
        newToDo.setTodoId(todoId);
        Toast.makeText(UploadToDoActivity.this, "Successfully added to your ToDo list!", Toast.LENGTH_SHORT).show();

        // Optionally handling Google Calendar integration
        handleGoogleCalendarEvent(account, newToDo, true);
    }

    // Updates an existing To-Do item with new details.
    private void updateExistingToDo(String name, String desc, String date, GoogleSignInAccount account) {
        editTodo.setName(name);
        editTodo.setDescription(desc);
        editTodo.setDate(date);

        // Attempting to update the To-Do item in the database
        boolean updated = db.updateToDo(editTodo);
        if (updated) {
            Toast.makeText(UploadToDoActivity.this, "ToDo has been updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(UploadToDoActivity.this, "ToDo update failed!", Toast.LENGTH_SHORT).show();
        }

        // Handling potential updates to the associated Google Calendar event
        handleGoogleCalendarEvent(account, editTodo, false);
    }

    // Manages the Google Calendar event associated with a To-Do item
    private void handleGoogleCalendarEvent(GoogleSignInAccount account, ToDo todo, boolean isNew) {
        //  return if not signed in to Google
        if (account == null) {
            Log.e("GoogleCalendarEvent", "GoogleSignInAccount is null, skipping event sync.");
            return;
        }
        if (!account.getGrantedScopes().contains(new Scope("https://www.googleapis.com/auth/calendar"))) {
            Toast.makeText(UploadToDoActivity.this, "No Google Calendar Permission!", Toast.LENGTH_SHORT).show();
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_CODE_PERMISSIONS,
                    account,
                    new Scope("https://www.googleapis.com/auth/calendar")
            );
        }


        // Initialize the GoogleCalendarService with the current account
        googleCalendarService = new GoogleCalendarService(UploadToDoActivity.this, account);
        if (isNew) {
            // Creating a new event in Google Calendar for the new To-Do item
            googleCalendarService.createAndAddEventToGoogleCalendar( todo,account, todo.getName(), todo.getDescription(), todo.getDate(),  new EventCallback() {
                @Override
                public void onEventAdded(String eventId) {
                    // Updating the To-Do item with the Google Calendar event ID
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
            // Updating an existing Google Calendar event for the To-Do item
            CompletableFuture.runAsync(() -> {
                try {
                    googleCalendarService.updateEvent(account, todo.getGoogleTodoId(), todo.getName(), todo.getDescription(), todo.getDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).thenRun(() -> {
                // This could be used to perform actions after the event update is completed
            });
            Toast.makeText(UploadToDoActivity.this, "ToDo has been updated in Google Calendar!", Toast.LENGTH_SHORT).show();
        }
    }

    // Navigates back to the main To-Do activity, passing along any necessary data.
    private void navigateToMainToDoActivity() {
        Intent intent = new Intent(UploadToDoActivity.this, ToDoMainActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("curr_date", curr_date);
        startActivity(intent);
    }
}


