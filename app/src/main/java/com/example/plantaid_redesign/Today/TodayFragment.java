package com.example.plantaid_redesign.Today;

import android.content.ClipData;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.Adapter.ReminderPlantAll_Adapter;
import com.example.plantaid_redesign.Common.Common;
import com.example.plantaid_redesign.LoginRegister.LoginRegisterActivity;
import com.example.plantaid_redesign.Model.PlantReminderModel;
import com.example.plantaid_redesign.Model.User;
import com.example.plantaid_redesign.Model.WeatherResult;
import com.example.plantaid_redesign.MyGarden.UserMyGardenPlantsActivity;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Retrofit.IOpenWeatherMap;
import com.example.plantaid_redesign.Retrofit.RetrofitClient;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shrikanthravi.collapsiblecalendarview.data.Day;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class TodayFragment extends Fragment {
    private static final String TAG = "home";
    private ImageView btnNavDrawer, imgTimeBg, currentWeatherIcon, imgProfile;
    private CardView weatherForecastCard;
    private TextView txtTemperature, txtLocation, txtCondition, txtDateTime, txtWelcome;
    private RelativeLayout weatherForecastView;
    private String txtSunrise, txtSunset, txtGeoCoords;
    private NavController navController;
    private String fname, lname, email;
    private CardView carddNoReminders;



    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private CollapsibleCalendar collapsibleCalendar;
    private ReminderPlantAll_Adapter cAdapter;
    private String plantCommonName, userKey;

    private ProgressBar loading;

    private CompositeDisposable compositeDisposable;
    private IOpenWeatherMap mService;
    private String selectedDate;

    private RecyclerView recyclerView;
    ArrayList<PlantReminderModel> list = new ArrayList<>();



//    public TodayFragment(){
//        compositeDisposable = new CompositeDisposable();
//        Retrofit retrofit = RetrofitClient.getInstance();
//        mService = retrofit.create(IOpenWeatherMap.class);
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_today, container, false);
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
        ((Home)getActivity()).updateStatusBarColor("#485E0D");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        btnNavDrawer = view.findViewById(R.id.btnNavDrawer);
        weatherForecastCard = view.findViewById(R.id.weatherForecastCard);
        imgTimeBg = view.findViewById(R.id.imgTimeBg);
        currentWeatherIcon = view.findViewById(R.id.currentWeatherIcon);
        txtTemperature = view.findViewById(R.id.txtTemperature);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtCondition = view.findViewById(R.id.txtCondition);
        txtDateTime = view.findViewById(R.id.txtDateTime);
        loading = view.findViewById(R.id.progressBar);
        carddNoReminders = view.findViewById(R.id.carddNoReminders);
        weatherForecastView = view.findViewById(R.id.weatherForecastView);
//        imgProfile = view.findViewById(R.id.imgProfile);
        txtWelcome = view.findViewById(R.id.txtWelcome);
        collapsibleCalendar = view.findViewById(R.id.collapsibleCalendar);
        recyclerView = view.findViewById(R.id.reyclerViewAllReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setHasFixedSize(true);
        cAdapter = new ReminderPlantAll_Adapter(list, getActivity());
        recyclerView.setAdapter(cAdapter);


        //Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        try{
            getWeatherInformation();
            openDrawer();
            greetUser();
            openWeatherForecastFragment();
            setBackgroundImage();
            selectCalendar();
            showReminders();

        }catch (Exception e){
            Log.e(TAG, "tab", e);
        }

    }

    public void openDrawer(){
        btnNavDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle result = new Bundle();
                result.putString("bundleKey", "clicked");
                // The child fragment needs to still set the result on its parent fragment manager
                getParentFragmentManager().setFragmentResult("requestKey", result);

            }
        });
    }


    public void showReminders(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("myGarden");
        databaseReference.child("Users").keepSynced(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    carddNoReminders.setVisibility(View.GONE);//noplants
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String key = dataSnapshot.getKey();
                        Log.d("Module_today", "onDataChange: 1    " + dataSnapshot.getKey());
                        for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                            Log.d("Module_today", "onDataChange: 2    " + dataSnapshot2.getKey());
                            for (DataSnapshot dataSnapshot3 : dataSnapshot2.getChildren()){
                                Log.d("Module_today", "onDataChange: 3    " + dataSnapshot3.getKey());
                                PlantReminderModel plantReminders = dataSnapshot3.getValue(PlantReminderModel.class);
                                list.add(plantReminders);


                            }
                        }
                    }
                    cAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast("Error");

            }
        });

    }


    public void selectCalendar(){

        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                String month = null;
                StringBuilder m = null;
                Day day = collapsibleCalendar.getSelectedDay();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    month = Month.of(day.getMonth() + 1).name().toLowerCase(Locale.ROOT);
                    m = new StringBuilder(month);
                    m.setCharAt(0, Character.toUpperCase(m.charAt(0)));
                }
                selectedDate = m + "_" + day.getDay() + ",_" + day.getYear();
//                toast(selectedDate);
            }

            @Override
            public void onDayChanged() {

            }

            @Override
            public void onClickListener() {

            }

            @Override
            public void onItemClick(View view) {

            }

            @Override
            public void onDataUpdate() {

            }

            @Override
            public void onMonthChange() {

            }

            @Override
            public void onWeekChange(int i) {

            }
        });
    }

    public void greetUser(){
        DatabaseReference reference = database.getReference("Users").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null){
                    fname = user.firstName;
                    lname = user.lastName;
                    email = user.email;

                    StringBuilder sb = new StringBuilder(fname);
                    sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
                    txtWelcome.setText("Hello, "+ sb.toString() + "!");
                    txtWelcome.setVisibility(View.VISIBLE);
                }
                else{
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    private void getWeatherInformation() {
        compositeDisposable.add(mService.getWeatherByLatLng(
                        String.valueOf(Common.current_location.getLatitude()),
                        String.valueOf(Common.current_location.getLongitude()),
                        Common.APP_ID,
                        "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {
                        //Load image
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                                .append(weatherResult.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(currentWeatherIcon);

                        txtLocation.setText(new StringBuilder("Weather in ")
                                .append(weatherResult.getName()).toString());
                        txtTemperature.setText(new StringBuilder(
                                String.valueOf((int)weatherResult.getMain().getTemp())).append("°C").toString());
                        txtDateTime.setText(Common.convertUnixToDate(weatherResult.getDt()));
                        txtSunrise = Common.convertUnixToHour(weatherResult.getSys().getSunrise());
                        txtSunset = Common.convertUnixToHour(weatherResult.getSys().getSunset());
                        txtGeoCoords = new StringBuilder("[").append(weatherResult.getCoord().toString()).append("]").toString();
                        txtCondition.setText(weatherResult.getWeather().get(0).getDescription());

                        //Log.d("weather",weatherResult.getName());
                        weatherForecastView.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    public void openWeatherForecastFragment(){
        weatherForecastCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Home)getActivity()).hideBottomNavigation(true);
                navController.navigate(R.id.action_todayFragment_to_weatherForecastFragment);
//                WeatherForecastFragment weatherForecastFragment= new WeatherForecastFragment();
//                getActivity().getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.body_container, weatherForecastFragment, "findThisFragment")
//                        .addToBackStack(null)
//                        .commit();
            }
        });
    }

    public void setBackgroundImage(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour < 12) {
            imgTimeBg.setImageResource(R.drawable.day_bg);
        } else if (hour >= 12 && hour < 17) {
            imgTimeBg.setImageResource(R.drawable.day_bg);
        } else if (hour >= 17 && hour < 20) {
            imgTimeBg.setImageResource(R.drawable.night_bg);
        } else {
            imgTimeBg.setImageResource(R.drawable.night_bg);
        }
    }

    private void toast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

}