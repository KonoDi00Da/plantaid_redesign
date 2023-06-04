package com.example.plantaid_redesign.MyGarden;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;

import com.example.plantaid_redesign.Model.PlantReminderModel;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Utilities.AlarmReceiver;
import com.github.hariprasanths.bounceview.BounceView;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    private TextView txtComPlantName, txtTask, txtCalendarDate, txtTime,txtDateRange;
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

    private LocalDate startLocalDate, endLocalDate;

    private List<LocalDate> selectedRangeDates;

    private Long startDate, endDate;

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
        txtDateRange = findViewById(R.id.txtDateRange);
        txtTime = findViewById(R.id.txtTime);
        edittxtCustomTask = findViewById(R.id.txtCustomTask);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);

        String date_ = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        date = new SimpleDateFormat("MMM_dd,_yyyy", Locale.getDefault()).format(new Date());

        txtCalendarDate.setText(formattedDate(selectedDate));

        txtTime.setText(formattedTime(time));

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
                    multipleAddDates();
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
                    multipleAddDates();
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
                showDialog();
            }
        });

        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime();
            }
        });

    }

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.cardview_date_picker);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnRange = dialog.findViewById(R.id.btnRange);
        Button btnSingle = dialog.findViewById(R.id.btnSingle);

        btnRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDateRangePicker();
                dialog.dismiss();
            }
        });

        btnSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker();
                dialog.dismiss();
            }
        });
        BounceView.addAnimTo(dialog);
        dialog.show();
    }

    private void multipleAddDates(){
        if(selectedRangeDates != null){
            setNotificationsForDateRange();
            toast("Reminder is now set");
        } else {
            addToFirebase();
            setNotification();
            toast("Reminder is now set");
        }
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
                    pushKey,
                    String.valueOf(notificationID),
                    String.valueOf(requestCode));

            userRef.child(pushKey).setValue(plantReminderModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    setNotification();
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

    private void materialDateRangePicker(){
        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker().setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds())).build();
        materialDatePicker.show(getSupportFragmentManager(),"DATE_PICKER");
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                Pair<Long, Long> selectedDates = (Pair<Long, Long>) selection;
                startDate = selectedDates.first;
                endDate = selectedDates.second;

                Instant instantStartDate = Instant.ofEpochMilli(startDate);
                startLocalDate = instantStartDate.atZone(ZoneId.systemDefault()).toLocalDate();

                Instant instantEndDate = Instant.ofEpochMilli(endDate);
                endLocalDate = instantEndDate.atZone(ZoneId.systemDefault()).toLocalDate();

                selectedRangeDates = new ArrayList<>();

                while(!startLocalDate.isAfter(endLocalDate)){
                    selectedRangeDates.add(startLocalDate);
                    startLocalDate = startLocalDate.plusDays(1);
                }

                txtCalendarDate.setText(materialDatePicker.getHeaderText());
            }
        });
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

    private void setNotificationsForDateRange() {
        if (hour == 0 && minute == 0) {
            hour = time.getHour();
            minute = time.getMinute();
        }



        // Create a calendar instance for the notification time (hour and minute)
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.set(Calendar.HOUR_OF_DAY, hour);
        notificationTime.set(Calendar.MINUTE, minute);
        notificationTime.set(Calendar.SECOND, 0);

        for (LocalDate date : selectedRangeDates) {
            selectedDate = date;
            // Convert LocalDate to Instant
            Instant instant = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Create a Calendar instance and set its time using the Instant
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(Date.from(instant));

            //add to firebase
            addToFirebase();

            // Set the notification text and other data
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("requestCode", requestCode);
            intent.putExtra("plantName", commonName);
            intent.putExtra("notificationID", notificationID);
            intent.putExtra("title", "PlantAid Reminder");
            intent.putExtra("customTask", customTask);
            intent.putExtra("task", task);
            intent.putExtra("delete", "null");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    PlantCare_Add_Reminder.this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // Set the notification time for each day within the date range
            Calendar notificationDateTime = Calendar.getInstance();
            notificationDateTime.set(Calendar.YEAR, startCalendar.get(Calendar.YEAR));
            notificationDateTime.set(Calendar.MONTH, startCalendar.get(Calendar.MONTH));
            notificationDateTime.set(Calendar.DAY_OF_MONTH, startCalendar.get(Calendar.DAY_OF_MONTH));

            notificationDateTime.set(Calendar.HOUR_OF_DAY, notificationTime.get(Calendar.HOUR_OF_DAY));
            notificationDateTime.set(Calendar.MINUTE, notificationTime.get(Calendar.MINUTE));
            notificationDateTime.set(Calendar.SECOND, 0);

            // Set the alarm for each day within the date range
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationDateTime.getTimeInMillis(), pendingIntent);

            Random random = new Random();
            requestCode = random.nextInt(9999 - 1000 + 1) + 1000;
            notificationID = random.nextInt(9999 - 1000 + 1) + 1000;
        }

        toast("Notifications set for the date range");
    }



    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}