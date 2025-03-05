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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class UploadToDoActivity extends AppCompatActivity {
    GoogleSignInClient googleSignInClient;
    EditText uploadName, uploadDesc;
    Button datePickerBtn, typePickerBtn;
    MaterialButton addNewTypeBtn, saveBtn, setTargetMinBtn;
    TextView targetTimeText;
    int targetMinutes;
    int typeId = -1;
    String typeName;
    private DatePickerDialog datePickerDialog;
    TextView mainTitle;

    String strUserId;
    String curr_date;
    int userId;
    int editTodoId;
    Bundle extras;
    dbConnectToDo db = new dbConnectToDo(this);
    dbConnectToDoType dbType = new dbConnectToDoType(this);
    GoogleCalendarService googleCalendarService;
    ToDo editTodo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_do);

        initDatePicker();

        uploadName = findViewById(R.id.addToDoName);
        uploadDesc = findViewById(R.id.addToDoDesc);
        setTargetMinBtn = findViewById(R.id.setTargetMinBtn);
        TextView targetTimeText = findViewById(R.id.targetTimeText);
        mainTitle = findViewById(R.id.mainTitle);
        addNewTypeBtn = findViewById(R.id.newtypebtn);
        saveBtn = findViewById(R.id.saveTodoBtn);
        datePickerBtn = findViewById(R.id.datePickerBtn);
        typePickerBtn = findViewById(R.id.typePickerBtn);
        targetMinutes = 0;


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
            typeId = editTodo.getTypeId();
            if(typeId!= -1) {
                typePickerBtn.setText(dbType.getToDoTypeById(typeId).getName());
            }
            targetMinutes = editTodo.getTargetMinutes();
            if(targetMinutes != 0) {
                targetTimeText.setText(": " + editTodo.getTargetMinutes() + " min.");
            }

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

        setTargetMinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Set a target time");

                    // Layout
                    LinearLayout layout = new LinearLayout(v.getContext());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(50, 40, 50, 10);

                    final EditText input = new EditText(v.getContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setHint("Give a number");
                    layout.addView(input);

                    // Spinner (Hour/Minute)
                    final Spinner spinner = new Spinner(v.getContext());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            new String[]{"Minutes", "Hours"});
                    spinner.setAdapter(adapter);
                    layout.addView(spinner);
                    // set layout for dialog
                    builder.setView(layout);

                    // OK
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String value = input.getText().toString();
                            String type = spinner.getSelectedItem().toString();

                            if (!value.isEmpty()) {
                                int number = Integer.parseInt(value);
                                int maxLimit = type.equals("Hours") ? 24 : 1440; // hour max 24, minute max 1440

                                if (number >= 0 && number <= maxLimit) {
                                    if( type.equals("Hours")) {
                                        targetMinutes = number * 60;
                                    }else{
                                        targetMinutes = number;
                                    }
                                    targetTimeText.setText(": " + targetMinutes + " min.");
                                } else {
                                    Toast.makeText(v.getContext(),
                                            "Invalid number! Limit for " + type + ": 0 - " + maxLimit,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                    // Cancel
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });



        datePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        addNewTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UploadToDoActivity.this, NewTypeActivity.class));
            }
        });

        typePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Type> typeList = dbType.getAllToDoTypesForUser(userId);
                // Spinner
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose a type for todo");

                final Spinner typeSpinner = new Spinner(v.getContext());
                ArrayAdapter<Type> adapter = new ArrayAdapter<>(v.getContext(),
                        android.R.layout.simple_spinner_dropdown_item, typeList);
                typeSpinner.setAdapter(adapter);

                // Layout
                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 40, 50, 10);
                layout.addView(typeSpinner);
                builder.setView(layout);

                // OK
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Type selectedType = (Type) typeSpinner.getSelectedItem();
                        typePickerBtn.setText(selectedType.getName());
                        typeId = selectedType.getTypeId();
                    }
                });

                // Cancel
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // Dialog
                builder.show();
            }
        });
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
                    createNewToDo(strToDoName, strToDoDesc, strDate, targetMinutes, typeId, account);
                } else {
                    updateExistingToDo(strToDoName, strToDoDesc, strDate, targetMinutes, typeId, account);
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
    private void createNewToDo(String name, String desc, String date, int targetMinutes, int typeId, GoogleSignInAccount account) {
        ToDo newToDo = new ToDo();
        newToDo.setName(name);
        newToDo.setDescription(desc);
        newToDo.setUserId(userId);
        newToDo.setDate(date);
        newToDo.setTargetMinutes(targetMinutes);
        newToDo.setDone(false);
        if(typeId != -1){
            newToDo.setTypeId(typeId);
        }

        // Adding the new To-Do to the database and retrieving its unique ID
        int todoId = db.addToDo(newToDo);
        newToDo.setTodoId(todoId);
        Toast.makeText(UploadToDoActivity.this, "Successfully added to your ToDo list!", Toast.LENGTH_SHORT).show();

        // Optionally handling Google Calendar integration
        handleGoogleCalendarEvent(account, newToDo, true);
    }

    // Updates an existing To-Do item with new details.
    private void updateExistingToDo(String name, String desc, String date, int targetMinutes, int typeId, GoogleSignInAccount account) {
        editTodo.setName(name);
        editTodo.setDescription(desc);
        editTodo.setDate(date);
        editTodo.setTargetMinutes(targetMinutes);
        if(typeId != -1){
            editTodo.setTypeId(typeId);
        }

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

        // Initialize the GoogleCalendarService with the current account
        googleCalendarService = new GoogleCalendarService(this, account);
        if (isNew) {
            // Creating a new event in Google Calendar for the new To-Do item
            googleCalendarService.createAndAddEventToGoogleCalendar(todo, account, todo.getName(), todo.getDescription(), todo.getDate(), new EventCallback() {
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


