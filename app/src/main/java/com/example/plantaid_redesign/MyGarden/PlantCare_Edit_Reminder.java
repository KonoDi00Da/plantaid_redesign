package com.example.plantaid_redesign.MyGarden;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

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

import com.example.plantaid_redesign.Model.PlantReminderModel;
import com.example.plantaid_redesign.Model.PlantRemindersStatistics;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import static com.example.plantaid_redesign.Utilities.DateUtils.formatDate;
import static com.example.plantaid_redesign.Utilities.DateUtils.formattedDate;
import static com.example.plantaid_redesign.Utilities.DateUtils.formattedTime;
import static com.example.plantaid_redesign.Utilities.DateUtils.localDateToCalendar;
import static com.example.plantaid_redesign.Utilities.DateUtils.selectedDate;

public class PlantCare_Edit_Reminder extends AppCompatActivity {
    public static final String TAG = "Reminder";
    private TextView editPlantName, editCalendarDate, editTime;
    private EditText editTask;
    private Button btnRemoveReminder,btnFinishReminder;
    private FloatingActionButton btnBack, btnAdd;


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    private String reminder_key, plant_name, oldTime, oldReminder, oldDate, user_key, requestCode, notificationID;

    public static String reminderKey;
    private String newReminder;
    private int year, month, day;
    private int hour, minute;
    private String task;

    private int water, repot, fertilize, custom;

    LocalTime time;

    @Nullable
    private Integer timeInputMode;

    private boolean isFinishClicked;

    private final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_care_edit_reminder);

        //Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        isFinishClicked = false;

        editPlantName = findViewById(R.id.editPlantName);
        editTask = findViewById(R.id.editTask);
        editCalendarDate = findViewById(R.id.editCalendarDate);
        editTime = findViewById(R.id.editTime);
        btnRemoveReminder = findViewById(R.id.btnRemoveReminder);
        btnFinishReminder = findViewById(R.id.btnFinishReminder);
        BounceView.addAnimTo(btnFinishReminder);
        BounceView.addAnimTo(btnRemoveReminder);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);

        try {
            getSetIntents();
            removeReminder();
            finishReminder();
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
                closeEditFragment();
            }
        });
    }

    private void closeEditFragment(){
        addToFirebase();
        setNotification();

        Intent intent = new Intent(PlantCare_Edit_Reminder.this, UserMyGardenPlantsActivity.class);
        //intent.putExtra("plant_image", model.getImage());
        intent.putExtra("commonName", plant_name);
        intent.putExtra("userKey", user_key);
        intent.putExtra("plantKey", UserMyGardenPlantsActivity.getPlantKeyStatic());
        intent.putExtra("page", "1");
        intent.putExtra("plant_image", UserMyGardenPlantsActivity.getImage());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void getSetIntents(){
        Intent intent = getIntent();
        plant_name = intent.getStringExtra("plant_name");
        oldTime = intent.getStringExtra("time");
        oldReminder = intent.getStringExtra("reminder");
        oldDate = intent.getStringExtra("date");
        user_key = intent.getStringExtra("user_Key");
        reminder_key = intent.getStringExtra("reminder_key");
        notificationID = intent.getStringExtra("notificationID");
        requestCode = intent.getStringExtra("requestCode");
        task = intent.getStringExtra("reminder");
        getStats();



        editPlantName.setText(plant_name);
        editTask.setText(oldReminder);
        editCalendarDate.setText(formattedDate(LocalDate.parse(oldDate)));
        editTime.setText(formattedTime(LocalTime.parse(oldTime)));

        selectedDate = LocalDate.parse(oldDate);
        time = LocalTime.parse(oldTime);
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

    private void finishReminder(){
        btnFinishReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCompletedDialog();
            }
        });
    }

    private void getStats(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("PlantStatistics").child(currentUser.getUid()).child(user_key);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PlantRemindersStatistics userPlants = snapshot.getValue(PlantRemindersStatistics.class);
                if(userPlants != null){
                    fertilize = userPlants.fertilize;
                    repot = userPlants.repot;
                    custom = userPlants.custom;
                    water = userPlants.water;

                    Log.d(TAG, "getSetIntents: " + water);
                    Log.d(TAG, "getSetIntents: " + custom);
                    Log.d(TAG, "getSetIntents: " + fertilize);
                    Log.d(TAG, "getSetIntents: " + repot);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast("Error");

            }

        });
    }

    private void addToFirebase() {
        DatabaseReference userRef = database.getReference().child("PlantReminders").child(currentUser.getUid());
        PlantReminderModel plantReminderModel = new PlantReminderModel(plant_name, newReminder,
                String.valueOf(selectedDate),
                String.valueOf(time), user_key, reminder_key, notificationID, requestCode,
                water, repot, fertilize, custom);

        userRef.child(reminder_key).setValue(plantReminderModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                toast("Reminder has been edited");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ", e);
                toast("Something went wrong");
            }
        });

    }

    private void statistics() {
        DatabaseReference userRef = database.getReference().child("PlantStatistics").child(currentUser.getUid());
        PlantRemindersStatistics plantReminderModel = new PlantRemindersStatistics(water, repot, fertilize, custom);

        userRef.child(user_key).setValue(plantReminderModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                toast("Reminder has been edited");
                isFinishClicked = false;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ", e);
                toast("Something went wrong");
            }
        });

    }


    private void showCompletedDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.cardview_complete_reminder);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //addcodesa firebase
                if(!isFinishClicked){
                    isFinishClicked = true;
                    switch (task){
                        case "Water":
                            water++;
                            break;
                        case "Fertilize":
                            fertilize++;
                            break;
                        case "Repot":
                            repot++;
                            break;
                        default:
                            custom++;
                            break;
                    }
                    newReminder = editTask.getText().toString().trim();
                    if (newReminder.isEmpty()) {
                        newReminder = editTask.getText().toString().trim();
                        editTask.setError("Text is required");
                        editTask.requestFocus();
                        return;
                    }
                    //addToFirebase();
                    statistics();
                    deleteReminder();
                    Intent intent = new Intent(PlantCare_Edit_Reminder.this, UserMyGardenPlantsActivity.class);
                    //intent.putExtra("plant_image", model.getImage());
                    intent.putExtra("commonName", plant_name);
                    intent.putExtra("userKey", user_key);
                    intent.putExtra("plantKey", UserMyGardenPlantsActivity.getPlantKeyStatic());
                    intent.putExtra("page", "1");
                    intent.putExtra("plant_image", UserMyGardenPlantsActivity.getImage());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                dialog.dismiss();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        BounceView.addAnimTo(dialog);
        dialog.show();
    }

    private void materialDateRangePicker(){
        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker().setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds())).build();
        materialDatePicker.show(getSupportFragmentManager(),"DATE_PICKER");
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                editCalendarDate.setText(materialDatePicker.getHeaderText());
            }
        });
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
        builder.setSelection(selectedDate.toEpochDay() * 24 * 60 * 60 * 1000);

        final MaterialDatePicker materialDatePicker = builder.build();
        materialDatePicker.show(getSupportFragmentManager(),"DATE_PICKER");

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                Instant instant = Instant.ofEpochMilli((Long) selection);
                selectedDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                editCalendarDate.setText(formatDate(selectedDate));

                year = localDateToCalendar(selectedDate).get(Calendar.YEAR);
                month = localDateToCalendar(selectedDate).get(Calendar.MONTH);
                day = localDateToCalendar(selectedDate).get(Calendar.DAY_OF_MONTH);

                toast("Date Selected: " + String.valueOf(selectedDate));
                //txtCalendarDate.setText(materialDatePicker.getHeaderText());
            }
        });
    }

    private void cancelNotification(){

        if (month == 0 && day == 0 && year == 0){
            year = localDateToCalendar(selectedDate).get(Calendar.YEAR);
            month = localDateToCalendar(selectedDate).get(Calendar.MONTH);
            day = localDateToCalendar(selectedDate).get(Calendar.DAY_OF_MONTH);
        }

        if (hour == 0 && minute == 0){
            hour = time.getHour();
            minute = time.getMinute();
        }

        Random random = new Random();
        int newRequestCode = random.nextInt(9999 - 1000 + 1) + 1000;

        //set notification id and text
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("requestCode", Integer.parseInt(requestCode));
        intent.putExtra("plantName", plant_name);
        intent.putExtra("notificationID", Integer.parseInt(notificationID));
        intent.putExtra("title", "PlantAid Reminder");
        intent.putExtra("customTask", oldReminder);
        intent.putExtra("task", task);
        intent.putExtra("delete", "null");

        //getBroadcast context, requestCode, intent, flags
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this,
                Integer.parseInt(requestCode),
                intent,
                PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);

        //Create time
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, hour);
        startTime.set(Calendar.MINUTE, minute);
        startTime.set(Calendar.SECOND, 0);

        startTime.set(Calendar.MONTH, month);
        startTime.set(Calendar.DAY_OF_MONTH, day);
        startTime.set(Calendar.YEAR, year);

        long alarmStart = startTime.getTimeInMillis();

        //set alarm
        //set type millisecond, intent
        alarmManager.cancel(alarmIntent);
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
        intent.putExtra("requestCode", Integer.parseInt(requestCode));
        intent.putExtra("plantName", plant_name);
        intent.putExtra("notificationID", Integer.parseInt(notificationID));
        intent.putExtra("title", "PlantAid Reminder");
        intent.putExtra("customTask", oldReminder);
        intent.putExtra("task", task);
        intent.putExtra("delete", "null");

        //getBroadcast context, requestCode, intent, flags
        PendingIntent pendingIntent = PendingIntent.getBroadcast(PlantCare_Edit_Reminder.this,
                Integer.parseInt(requestCode),
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
                editTime.setText(formattedTime(time));
            }
        });
    }

    private void deleteReminder() {
        DatabaseReference userRef = database.getReference().child("PlantReminders").child(currentUser.getUid()).child(reminder_key);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userRef.removeValue();
                cancelNotification();
                Intent intent = new Intent(PlantCare_Edit_Reminder.this, UserMyGardenPlantsActivity.class);
                //intent.putExtra("plant_image", model.getImage());
                intent.putExtra("commonName", plant_name);
                intent.putExtra("userKey", user_key);
                intent.putExtra("plantKey", UserMyGardenPlantsActivity.getPlantKeyStatic());
                intent.putExtra("page", "1");
                intent.putExtra("plant_image", UserMyGardenPlantsActivity.getImage());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        toast("Reminder cancelled");

    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}