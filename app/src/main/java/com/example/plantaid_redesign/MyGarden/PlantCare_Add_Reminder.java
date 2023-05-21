package com.example.plantaid_redesign.MyGarden;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.plantaid_redesign.Model.PlantReminderModel;
import com.example.plantaid_redesign.R;
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
import java.util.Date;
import java.util.Locale;

public class PlantCare_Add_Reminder extends AppCompatActivity {
    public static final String TAG = "Reminder";
    private Calendar cal;
    private TextView txtComPlantName, txtTask, txtCalendarDate, txtTime;
    private EditText edittxtCustomTask;
    private FloatingActionButton btnBack, btnAdd;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    //calendar
    private int hour, minute;
    private int year, month, day;
    private int requestCode, notificationID, currentHour,currentMinutes;
    private String timeFormat,commonName, userKey, task, date, dateFormatted, userTask, customTask;



    @TimeFormat private int clockFormat;
    @Nullable private Integer timeInputMode;


    private final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_care_add_reminder);
        cal = Calendar.getInstance();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        btnBack = findViewById(R.id.btnBack);
        txtComPlantName = findViewById(R.id.txtComPlantName);
        txtTask = findViewById(R.id.txtTask);
        txtCalendarDate = findViewById(R.id.txtCalendarDate);
        txtTime = findViewById(R.id.txtTime);
        edittxtCustomTask = findViewById(R.id.txtCustomTask);
        btnAdd = findViewById(R.id.btnAdd);

        try {
            txtComPlantName.setText(getIntent().getStringExtra("plantCommonName"));
            setCurrentTimeDate();
            onBack();
            selectDate();
            selectTime();
            setUserTask();

        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }

    }
    private void addToFirebase(){
        userKey = getIntent().getStringExtra("userKey");
        commonName = getIntent().getStringExtra("plantCommonName");
        DatabaseReference userRef = database.getReference("Users").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String plantID = "myGarden";
                String reminders = "plantReminders";
                String pushKey = userRef.push().getKey();
                PlantReminderModel plantReminderModel = new PlantReminderModel(commonName,task,date,timeFormat, userKey, pushKey);
                userRef.child(plantID).child(userKey).child(reminders).child(pushKey).setValue(plantReminderModel);
                toast("Reminder is now set");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectDate() {
        txtCalendarDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDatePicker();
            }
        });
    }
    private void selectTime() {
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime();
            }
        });
    }

    public void setUserTask(){
        userTask = getIntent().getStringExtra("taskReminder");

        if(userTask.equals("Custom")){
            edittxtCustomTask.setVisibility(View.VISIBLE);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customTask = edittxtCustomTask.getText().toString().trim();
                    task = customTask;
                    if (customTask.isEmpty()) {
                        customTask = edittxtCustomTask.getText().toString().trim();
                        edittxtCustomTask.setError("Text is required");
                        edittxtCustomTask.requestFocus();
                        return;
                    }
                    addToFirebase();
                    finish();
                }
            });
        }else{
            task = userTask;
            txtTask.setText(task);
            txtTask.setVisibility(View.VISIBLE);

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToFirebase();
//                    setNotification();
                    finish();
                }
            });
        }

    }




    public void onBack(){
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void materialDatePicker()
    {
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now());

        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker().setCalendarConstraints(constraintBuilder.build());
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        final MaterialDatePicker materialDatePicker = builder.build();
        materialDatePicker.show(getSupportFragmentManager(),"DATE_PICKER");

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                date = materialDatePicker.getHeaderText().replace(" ","_");
                String selectedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).toString();
                cal.setTimeInMillis((Long) selection);
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH) + 1;
                day = cal.get(Calendar.DAY_OF_MONTH);

                toast("Date Selected: " + year + month + day);
                txtCalendarDate.setText(materialDatePicker.getHeaderText());
            }
        });
    }

    private void setCurrentTimeDate(){
        String date_ = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        date = new SimpleDateFormat("MMM_dd,_yyyy", Locale.getDefault()).format(new Date());
        txtCalendarDate.setText(date_);
        txtCalendarDate.setText(date_);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat time = new SimpleDateFormat("h:mm a");
        SimpleDateFormat time_ = new SimpleDateFormat("h:mm_a");


        String currentTime = time.format(calendar.getTime());
        String currentTime_ = time_.format(calendar.getTime());
        txtTime.setText(currentTime);
        timeFormat = currentTime_;
    }

    private void setTime() {
        clockFormat = TimeFormat.CLOCK_12H;

        MaterialTimePicker.Builder materialTimePickerBuilder = new MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(currentHour)
                .setMinute(currentMinutes);


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
                PlantCare_Add_Reminder.this.onTimeSet(newHour, newMinute);
            }
        });
    }

    private void onTimeSet(int newHour, int newMinute) {
        cal.set(Calendar.HOUR_OF_DAY, newHour);
        cal.set(Calendar.MINUTE, newMinute);
        cal.setLenient(false);
        String time = formatter.format(cal.getTime());

        timeFormat = time.replace(" ","_");
        txtTime.setText(timeFormat);
        hour = newHour;
        minute = newMinute;
    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}