package com.example.plantaid_redesign.MyGarden;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.Model.PlantReminderModel;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.github.hariprasanths.bounceview.BounceView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PlantCare_Edit_Reminder extends AppCompatActivity {
    public static final String TAG = "Reminder";
    private TextView editPlantName, editCalendarDate, editTime;
    private EditText editTask;
    private Button btnRemoveReminder;
    private FloatingActionButton btnBack, btnAdd;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    private String reminder_key, plant_name, oldTime, oldReminder, oldDate, user_key;
    private String newTime, newReminder,newDate;
    private int hour, minute;
    private String timeFormat, task, customTask;

    @TimeFormat
    private int clockFormat;
    @Nullable
    private Integer timeInputMode;

    private final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_care_edit_reminder);

        clockFormat = TimeFormat.CLOCK_12H;

        //Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        editPlantName = findViewById(R.id.editPlantName);
        editTask = findViewById(R.id.editTask);
        editCalendarDate = findViewById(R.id.editCalendarDate);
        editTime = findViewById(R.id.editTime);
        btnRemoveReminder = findViewById(R.id.btnRemoveReminder);
        BounceView.addAnimTo(btnRemoveReminder);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);

        try {
            getSetIntents();
            removeReminder();
            selectDate();
            selectTime();
            setUserTask();
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }

    private void setUserTask() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newReminder = editTask.getText().toString().trim();
                if (newReminder.isEmpty()) {
                    newReminder = editTask.getText().toString().trim();
                    editTask.setError("Text is required");
                    editTask.requestFocus();
                    return;
                }
                addToFirebase();
                finish();

            }
        });
    }

    public void getSetIntents(){
        Intent intent = getIntent();
        plant_name = intent.getStringExtra("plant_name");
        oldTime = intent.getStringExtra("time");
        oldReminder = intent.getStringExtra("reminder");
        oldDate = intent.getStringExtra("date");
        user_key = intent.getStringExtra("user_Key");
        reminder_key = intent.getStringExtra("reminder_key");

        editPlantName.setText(plant_name);
        editTask.setText(oldReminder);
        editCalendarDate.setText(oldDate.replace("_"," "));
        editTime.setText(oldTime.replace("_"," "));

        newTime = oldTime;
        newDate = oldDate;
    }

    public void removeReminder(){
        btnRemoveReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(PlantCare_Edit_Reminder.this);
                dialog.setContentView(R.layout.remove_reminder_plant);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(true);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                Button btnDelete = dialog.findViewById(R.id.btnDelete);
                Button btnCancel = dialog.findViewById(R.id.btnCancel);

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteReminder();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
//                Toast.makeText(MainActivity.this, "Cancel clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                BounceView.addAnimTo(dialog);
                dialog.show();
            }
        });
    }
    private void addToFirebase() {
        DatabaseReference userRef = database.getReference("Users").child(currentUser.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String plantID = "myGarden";
                String reminders = "plantReminders";
                PlantReminderModel plantReminderModel = new PlantReminderModel(plant_name,newReminder,newDate,newTime, user_key, reminder_key);
                userRef.child(plantID).child(user_key).child(reminders).child(reminder_key).setValue(plantReminderModel);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        toast("Reminder has been edited");
    }
    private void selectDate() {
        editCalendarDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDatePicker();
            }
        });
    }
    private void selectTime() {
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime();
            }
        });
    }

    private void materialDatePicker(){
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now());

        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker().setCalendarConstraints(constraintBuilder.build());
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        final MaterialDatePicker materialDatePicker = builder.build();
        materialDatePicker.show(getSupportFragmentManager(),"DATE_PICKER");

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                newDate = materialDatePicker.getHeaderText().replace(" ","_");
                editCalendarDate.setText(materialDatePicker.getHeaderText());
            }
        });
    }

    private void setTime() {
        MaterialTimePicker.Builder materialTimePickerBuilder = new MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(hour)
                .setMinute(minute);

        if (timeInputMode != null) {
            materialTimePickerBuilder.setInputMode(timeInputMode);
        }

        MaterialTimePicker materialTimePicker = materialTimePickerBuilder.build();
        materialTimePicker.show(getSupportFragmentManager(), "TIME_PICKER");

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newHour = materialTimePicker.getHour();
                int newMinute = materialTimePicker.getMinute();
                PlantCare_Edit_Reminder.this.onTimeSet(newHour, newMinute);
            }
        });
    }

    private void onTimeSet(int newHour, int newMinute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, newHour);
        cal.set(Calendar.MINUTE, newMinute);
        cal.setLenient(false);

        String time = formatter.format(cal.getTime());
        newTime = time.replace(" ","_");
        editTime.setText(time);
        hour = newHour;
        minute = newMinute;
    }

    private void deleteReminder() {
        DatabaseReference userRef = database.getReference("Users").child(currentUser.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String plantID = "myGarden";
                userRef.child(plantID).child(user_key).child("plantReminders").child(reminder_key).removeValue();
                toast("Reminder Deleted");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        finish();
    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}