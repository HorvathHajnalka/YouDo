package com.example.youdo.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.youdo.Database.dbConnectToDo;
import com.example.youdo.Database.dbConnectToDoType;
import com.example.youdo.HelperServices.StepCounterHelper.EventCallback;
import com.example.youdo.HelperServices.GoogleCalendarService;
import com.example.youdo.Models.ToDo;
import com.example.youdo.Models.Type;
import com.example.youdo.R;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import yuku.ambilwarna.AmbilWarnaDialog;
public class UploadToDoActivity extends AppCompatActivity {
    GoogleSignInClient googleSignInClient;
    EditText uploadName, uploadDesc;
    Button datePickerBtn, typePickerBtn;
    MaterialButton addNewTypeBtn, saveBtn, setTargetMinBtn, delTypeBtn;
    int targetMinutes;
    int typeId = -1;
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
        delTypeBtn = findViewById(R.id.deltypebtn);
        saveBtn = findViewById(R.id.saveTodoBtn);
        datePickerBtn = findViewById(R.id.datePickerBtn);
        typePickerBtn = findViewById(R.id.typePickerBtn);
        targetMinutes = 0;

        extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getInt("userId", -1);
            strUserId = String.valueOf(userId);
            editTodoId = extras.getInt("todoId", -1);
            curr_date = extras.getString("curr_date", "-1");
        }

        // check if we're editing an existing To-Do
        if (editTodoId != -1) {
            editTodo = db.getTodoById(editTodoId);
            mainTitle.setText("Edit ToDo");
            uploadName.setText(editTodo.getName());
            uploadDesc.setText(editTodo.getDescription());
            typeId = editTodo.getTypeId();
            Type selectedType = dbType.getToDoTypeById(typeId);
            if (selectedType != null) {
                typePickerBtn.setText(selectedType.getName());
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

                    LinearLayout layout = new LinearLayout(v.getContext());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(50, 40, 50, 10);

                    final EditText input = new EditText(v.getContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setHint("Give a number");
                    layout.addView(input);

                    // spinner (Hour/Minute)
                    final Spinner spinner = new Spinner(v.getContext());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            new String[]{"Minutes", "Hours"});
                    spinner.setAdapter(adapter);
                    layout.addView(spinner);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(UploadToDoActivity.this);
                builder.setTitle("Add New Type");

                LinearLayout layout = new LinearLayout(UploadToDoActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 40, 50, 10);

                final EditText typeNameInput = new EditText(UploadToDoActivity.this);
                typeNameInput.setHint("Type Name");
                layout.addView(typeNameInput);

                // open color picker
                final Button colorPickerBtn = new Button(UploadToDoActivity.this);
                colorPickerBtn.setText("Pick a Color");
                layout.addView(colorPickerBtn);

                // display selected color
                final TextView colorPreview = new TextView(UploadToDoActivity.this);
                colorPreview.setText("Selected Color");
                colorPreview.setPadding(10, 20, 10, 20);
                colorPreview.setTextColor(Color.parseColor("#ffffff"));
                layout.addView(colorPreview);

                // checkbox for reward condition
                final CheckBox rewardCheckBox = new CheckBox(UploadToDoActivity.this);
                rewardCheckBox.setText("Reward if completed in less time");
                layout.addView(rewardCheckBox);

                builder.setView(layout);

                // color picker event handler
                final int[] selectedColor = {Color.BLACK}; // default color
                colorPickerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AmbilWarnaDialog(UploadToDoActivity.this, selectedColor[0], new AmbilWarnaDialog.OnAmbilWarnaListener() {
                            @Override
                            public void onOk(AmbilWarnaDialog dialog, int color) {
                                selectedColor[0] = color;
                                colorPreview.setBackgroundColor(color);
                            }

                            @Override
                            public void onCancel(AmbilWarnaDialog dialog) {
                            }
                        }).show();
                    }
                });

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String typeName = typeNameInput.getText().toString().trim();
                        if (!typeName.isEmpty()) {
                            Type newType = new Type();
                            newType.setName(typeName);
                            newType.setColour("#" + Integer.toHexString(selectedColor[0]));
                            newType.setUserId(userId);
                            newType.setSumTargetMinutes(0);
                            newType.setSumAchievedMinutes(0);
                            newType.setRewardOverAchievement(!rewardCheckBox.isChecked());
                            typeId = dbType.addToDoType(newType);
                            typePickerBtn.setText(newType.getName());
                            // Toast.makeText(UploadToDoActivity.this, "Type: " + typeName + ", Color: #" + Integer.toHexString(selectedColor[0]), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UploadToDoActivity.this, "Please enter a name!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });


        delTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ask the user which type to delete
                AlertDialog.Builder builder = new AlertDialog.Builder(UploadToDoActivity.this);
                builder.setTitle("Delete Type");

                // dropdown to select the type to delete
                final Spinner typeSpinner = new Spinner(UploadToDoActivity.this);
                List<Type> typeList = dbType.getAllUserToDoTypesForUser(userId);
                ArrayAdapter<Type> adapter = new ArrayAdapter<>(UploadToDoActivity.this, android.R.layout.simple_spinner_dropdown_item, typeList);
                typeSpinner.setAdapter(adapter);

                builder.setView(typeSpinner);

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Type selectedType = (Type) typeSpinner.getSelectedItem();

                        if (selectedType != null) {
                            // check if the type has any associated To-Do items
                            boolean hasAssociatedTodos = dbType.hasToDosAssigned(selectedType.getTypeId());
                            if (hasAssociatedTodos) {
                                Toast.makeText(UploadToDoActivity.this, "This type cannot be deleted because it has associated ToDos.", Toast.LENGTH_SHORT).show();
                            }else {
                                dbType.deleteToDoTypeById(selectedType.getTypeId());
                                Toast.makeText(UploadToDoActivity.this, "Type '" + selectedType + "' has been deleted.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(UploadToDoActivity.this, "Type not found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        typePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Type> typeList = dbType.getAllToDoTypesForUser(userId);
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose a type for todo");

                final Spinner typeSpinner = new Spinner(v.getContext());
                ArrayAdapter<Type> adapter = new ArrayAdapter<>(v.getContext(),
                        android.R.layout.simple_spinner_dropdown_item, typeList);
                typeSpinner.setAdapter(adapter);

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

                // checking if the user is signed in with Google
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

        // month in Java's Calendar class is zero-based so it needs to be incremented by one to get the correct display value.
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
        // setting the minimum date to the current date, preventing selection of past dates.
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());


    }

    // formats the date string by converting the month integer to a string abbreviation and appending it with the day and year.
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

    // retrieves the selected date from the DatePicker and sets it as the start time in a Calendar instance.
    private Calendar getStartTime() {
        DatePicker datePicker = datePickerDialog.getDatePicker();
        Calendar startTime = Calendar.getInstance();
        startTime.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0);
        return startTime;
    }

    // formats a Calendar instance to a string
    private String getFormattedDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime());
    }

    private void createNewToDo(String name, String desc, String date, int targetMinutes, int typeId, GoogleSignInAccount account) {
        ToDo newToDo = new ToDo();
        newToDo.setName(name);
        newToDo.setDescription(desc);
        newToDo.setUserId(userId);
        newToDo.setDate(date);
        newToDo.setTargetMinutes(targetMinutes);
        newToDo.setDone(false);
        if(typeId != -1 && dbType.getToDoTypeById(typeId) != null){
            newToDo.setTypeId(typeId);
            Type todoType = dbType.getToDoTypeById(typeId);
            todoType.setSumTargetMinutes(todoType.getSumTargetMinutes() + targetMinutes);
        }

        int todoId = db.addToDo(newToDo);
        newToDo.setTodoId(todoId);
        Toast.makeText(UploadToDoActivity.this, "Successfully added to your ToDo list!", Toast.LENGTH_SHORT).show();

        // handling Google Calendar integration
        handleGoogleCalendarEvent(account, newToDo, true);
    }

    private void updateExistingToDo(String name, String desc, String date, int targetMinutes, int typeId, GoogleSignInAccount account) {
        editTodo.setName(name);
        editTodo.setDescription(desc);
        editTodo.setDate(date);
        editTodo.setTargetMinutes(targetMinutes);
        if(typeId != -1 && dbType.getToDoTypeById(typeId) != null){
            editTodo.setTypeId(typeId);
        }

        boolean updated = db.updateToDo(editTodo);
        if (updated) {
            Toast.makeText(UploadToDoActivity.this, "ToDo has been updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(UploadToDoActivity.this, "ToDo update failed!", Toast.LENGTH_SHORT).show();
        }

        handleGoogleCalendarEvent(account, editTodo, false);
    }

    // manages the Google Calendar event associated with a To-Do item
    private void handleGoogleCalendarEvent(GoogleSignInAccount account, ToDo todo, boolean isNew) {
        //  return if not signed in to Google
        if (account == null) {
            Log.e("GoogleCalendarEvent", "GoogleSignInAccount is null, skipping event sync.");
            return;
        }

        // initialize the GoogleCalendarService with the current account
        googleCalendarService = new GoogleCalendarService(this, account);
        if (isNew) {
            // creating a new event in Google Calendar
            googleCalendarService.createAndAddEventToGoogleCalendar(todo, account, todo.getName(), todo.getDescription(), todo.getDate(), new EventCallback() {
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
            // updating an existing Google Calendar event for the To-Do item
            CompletableFuture.runAsync(() -> {
                try {
                    googleCalendarService.updateEvent(account, todo.getGoogleTodoId(), todo.getName(), todo.getDescription(), todo.getDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).thenRun(() -> { });
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


