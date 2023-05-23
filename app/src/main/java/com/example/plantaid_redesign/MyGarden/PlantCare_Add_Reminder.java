package com.example.plantaid_redesign.MyGarden;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.plantaid_redesign.Model.PlantReminderModel;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Utilities.AlarmReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static com.example.plantaid_redesign.Utilities.DateUtils.formatDate;
import static com.example.plantaid_redesign.Utilities.DateUtils.formattedDate;
import static com.example.plantaid_redesign.Utilities.DateUtils.formattedTime;
import static com.example.plantaid_redesign.Utilities.DateUtils.localDateToCalendar;
import static com.example.plantaid_redesign.Utilities.DateUtils.selectedDate;

public class PlantCare_Add_Reminder extends AppCompatActivity {
    public static final String TAG = "Reminder";
    private Calendar cal;

    private TextView txtComPlantName, txtTask, txtCalendarDate, txtTime;
    private EditText edittxtCustomTask;
    private FloatingActionButton btnBack, btnAdd;
    private Button btnOK, btnCancel;
    String customTask;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;

    private int hour, minute;
    private int year, month, day;

    int requestCode, notificationID, currentHour,currentMinutes;
    String eventNameTxt;
    private LocalTime time;

    private String timeFormat,commonName, userKey, task, date, dateFormatted,custommTask;

    @TimeFormat  private int clockFormat;
    @Nullable private Integer timeInputMode;

    private DatabaseReference userRef;
    private final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_care_add_reminder);
//        createNotificationChannel();
        cal = Calendar.getInstance();
        time = LocalTime.now();
        selectedDate = LocalDate.now();

        Random random = new Random();
        requestCode = random.nextInt(9999 - 1000 + 1) + 1000;
        notificationID = random.nextInt(9999 - 1000 + 1) + 1000;

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference().child("PlantReminders").child(currentUser.getUid());

        txtComPlantName = findViewById(R.id.txtComPlantName);
        txtTask = findViewById(R.id.txtTask);
        txtCalendarDate = findViewById(R.id.txtCalendarDate);
        txtTime = findViewById(R.id.txtTime);
        edittxtCustomTask = findViewById(R.id.txtCustomTask);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);

        String date_ = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        date = new SimpleDateFormat("MMM_dd,_yyyy", Locale.getDefault()).format(new Date());

        txtCalendarDate.setText(formattedDate(selectedDate));

        //currentHour = cal.get(Calendar.HOUR);
        //hour = currentMinutes;
        //currentMinutes = cal.get(Calendar.MINUTE);
        //minute = currentMinutes;

        //Calendar calendar = Calendar.getInstance();
        //SimpleDateFormat time = new SimpleDateFormat("h:mm a");
        //SimpleDateFormat time_ = new SimpleDateFormat("h:mm_a");

        //String currentTime = time.format(calendar.getTime());
        //String currentTime_ = time_.format(calendar.getTime());
        txtTime.setText(formattedTime(time));
        //timeFormat = currentTime_;

        String userTask = getIntent().getStringExtra("taskReminder");
        commonName = getIntent().getStringExtra("plantCommonName");
        userKey = getIntent().getStringExtra("userKey");

        txtComPlantName.setText(commonName);

        clockFormat = TimeFormat.CLOCK_12H;

        if(userTask.equals("Custom")){
            edittxtCustomTask.setVisibility(View.VISIBLE);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customTask = edittxtCustomTask.getText().toString().trim();
                    custommTask = "custom";
                    task = customTask;
                    if (customTask.isEmpty()) {
                        customTask = edittxtCustomTask.getText().toString().trim();
                        edittxtCustomTask.setError("Text is required");
                        edittxtCustomTask.requestFocus();
                        return;
                    }
                    addToFirebase();
                    setNotification();
                    Intent intent = new Intent(PlantCare_Add_Reminder.this, UserMyGardenPlantsActivity.class);
                    //intent.putExtra("plant_image", model.getImage());
                    intent.putExtra("commonName", commonName);
                    intent.putExtra("userKey", userKey);
                    intent.putExtra("plantKey", UserMyGardenPlantsActivity.getPlantKeyStatic());
                    intent.putExtra("page", "1");
                    intent.putExtra("plant_image", UserMyGardenPlantsActivity.getImage());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }else{
            custommTask = "nope";
            task = userTask;
            txtTask.setText(task);
            txtTask.setVisibility(View.VISIBLE);

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToFirebase();
                    setNotification();
                    Intent intent = new Intent(PlantCare_Add_Reminder.this, UserMyGardenPlantsActivity.class);
                    //intent.putExtra("plant_image", model.getImage());
                    intent.putExtra("commonName", commonName);
                    intent.putExtra("userKey", userKey);
                    intent.putExtra("plantKey", UserMyGardenPlantsActivity.getPlantKeyStatic());
                    intent.putExtra("page", "1");
                    intent.putExtra("plant_image", UserMyGardenPlantsActivity.getImage());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtCalendarDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker();
            }
        });

        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime();
            }
        });
    }

    private void addToFirebase() {
        try{
            String pushKey = userRef.push().getKey();
            PlantReminderModel plantReminderModel = new PlantReminderModel(
                    commonName,
                    task,
                    String.valueOf(selectedDate),
                    String.valueOf(time),
                    userKey,
                    pushKey);

            userRef.child(pushKey).setValue(plantReminderModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    toast("Reminder is now set");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    toast("Something went wrong");
                    Log.e(TAG, "onComplete: ", e);
                }
            });
        }catch (Exception e){
            toast("Something went wrong, please try again");
            Log.e("SaveReminder", "exception", e);
        }
    }


    private void materialDatePicker(){
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now());

        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker().setCalendarConstraints(constraintBuilder.build());
        builder.setSelection(selectedDate.toEpochDay() * 24 * 60 * 60 * 1000);
        final MaterialDatePicker materialDatePicker = builder.build();
        materialDatePicker.show(getSupportFragmentManager(),"DATE_PICKER");

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                Instant instant = Instant.ofEpochMilli((Long) selection);
                selectedDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                txtCalendarDate.setText(formatDate(selectedDate));

                date = materialDatePicker.getHeaderText().replace(" ","_");
                //String selectedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).toString();
                cal.setTimeInMillis((Long) selection);
                year = localDateToCalendar(selectedDate).get(Calendar.YEAR);
                month = localDateToCalendar(selectedDate).get(Calendar.MONTH);
                day = localDateToCalendar(selectedDate).get(Calendar.DAY_OF_MONTH);

                toast("Date Selected: " + String.valueOf(selectedDate));
                //txtCalendarDate.setText(materialDatePicker.getHeaderText());
            }
        });
    }

    private void setTime() {
        MaterialTimePicker.Builder materialTimePickerBuilder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(time.getHour())
                .setMinute(time.getMinute());


        if (timeInputMode != null) {
            materialTimePickerBuilder.setInputMode(timeInputMode);
        }

        MaterialTimePicker materialTimePicker = materialTimePickerBuilder.build();
        materialTimePicker.show(getSupportFragmentManager(), "TIME_PICKER");

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hour = materialTimePicker.getHour();
                minute = materialTimePicker.getMinute();
                time = LocalTime.of(hour, minute);
                txtTime.setText(formattedTime(time));
            }
        });
    }

    private void onTimeSet(int newHour, int newMinute) {
        cal.set(Calendar.HOUR_OF_DAY, newHour);
        cal.set(Calendar.MINUTE, newMinute);
        cal.setLenient(false);

        String time = formatter.format(cal.getTime());
        timeFormat = time.replace(" ","_");
        txtTime.setText(time);
        hour = newHour;
        minute = newMinute;
    }


    private void setNotification(){
        if (month == 0 && day == 0 && year == 0){
            year = localDateToCalendar(selectedDate).get(Calendar.YEAR);
            month = localDateToCalendar(selectedDate).get(Calendar.MONTH);
            day = localDateToCalendar(selectedDate).get(Calendar.DAY_OF_MONTH);
        }

        if (hour == 0 && minute == 0){
            hour = time.getHour();
            minute = time.getMinute();
        }


        //set notification id and text
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("requestCode", requestCode);
        intent.putExtra("plantName", commonName);
        intent.putExtra("notificationID", notificationID);
        intent.putExtra("title", "PlantAid Reminder");
        intent.putExtra("customTask", custommTask);
        intent.putExtra("task", task);
        intent.putExtra("delete", "null");

        //getBroadcast context, requestCode, intent, flags
        PendingIntent pendingIntent = PendingIntent.getBroadcast(PlantCare_Add_Reminder.this,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Create time
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, hour);
        startTime.set(Calendar.MINUTE, minute);
        startTime.set(Calendar.SECOND, 0);

        startTime.set(Calendar.MONTH, month);
        startTime.set(Calendar.DAY_OF_MONTH, day);
        startTime.set(Calendar.YEAR, year);


        //set alarm
        //set type millisecond, intent
        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(), pendingIntent);
        toast("Notification set");
    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}