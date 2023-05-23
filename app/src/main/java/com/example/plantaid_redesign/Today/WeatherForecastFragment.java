package com.example.plantaid_redesign.Today;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.plantaid_redesign.Adapter.WeatherForecastAdapter;
import com.example.plantaid_redesign.Common.Common;
import com.example.plantaid_redesign.Model.WeatherForecastResult;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Retrofit.IOpenWeatherMap;
import com.example.plantaid_redesign.Retrofit.RetrofitClient;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class WeatherForecastFragment extends Fragment implements  BackpressedListener{
    public static final String TAG = "WeatherForecast";
    private ImageView btn_back, time_bg, currentWeatherIcon;
    private TextView txtLocation, txtCondition, txtTemperature, txtDayAndDate, txtMessage;
    private RecyclerView rec_forecast;
    private RelativeLayout weatherForecastView;

    private CompositeDisposable compositeDisposable;
    private IOpenWeatherMap mService;
    private NavController navController;

    static WeatherForecastFragment instance;

//    public static  WeatherForecastFragment getInstance(){
//        if(instance == null)
//            instance = new WeatherForecastFragment();
//            return instance;
//    }

    public WeatherForecastFragment(){
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_weather_forecast, container, false);

        return itemView;

    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        btn_back = view.findViewById(R.id.btn_back);
        time_bg = view.findViewById(R.id.time_bg);
        currentWeatherIcon = view.findViewById(R.id.currentWeatherIcon);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtCondition = view.findViewById(R.id.txtCondition);
        txtTemperature = view.findViewById(R.id.txtTemperature);
        txtDayAndDate = view.findViewById(R.id.txtDayAndDate);
        rec_forecast = view.findViewById(R.id.rec_forecast);
        weatherForecastView = view.findViewById(R.id.weatherForecastView);
        txtMessage = view.findViewById(R.id.txtMessage);

        try {
            rec_forecast.setHasFixedSize(true);
            rec_forecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            getForecastWeatherInformation();
            setBackgroundImage();
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.action_weatherForecastFragment_to_todayFragment);
                    ((Home)getActivity()).hideBottomNavigation(false);
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "recview", e);
        }


    }

    private void getForecastWeatherInformation() {
        compositeDisposable.add(mService.getForecastWeatherByLtLng(
                String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,
                "metric")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<WeatherForecastResult>() {
                            @Override
                            public void accept(WeatherForecastResult weatherForecastResult) throws Exception {
                                displayForecastWeather(weatherForecastResult);

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d("ERROR", ""+throwable.getMessage());

                            }
                        })
                );
    }

    private void displayForecastWeather(WeatherForecastResult weatherForecastResult) {
        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                .append(weatherForecastResult.list.get(0).weather.get(0).getIcon())
                .append(".png").toString()).into(currentWeatherIcon);
        txtLocation.setText(new StringBuilder(weatherForecastResult.city.name));
        txtTemperature.setText(new StringBuilder(String.valueOf((int)weatherForecastResult.list.get(0).main.getTemp())).append("°C"));
        txtDayAndDate.setText(new StringBuilder(Common.convertUnixToDate(weatherForecastResult.list.get(0).dt)));
        String condition = String.valueOf(new StringBuilder(weatherForecastResult.list.get(0).weather.get(0).getDescription()));
        txtCondition.setText(condition);
        switch (condition.toLowerCase(Locale.ROOT)){
            case "cloudy":
                txtMessage.setText(R.string.cloudy_day);
            case "light rain":
                txtMessage.setText(R.string.sunny_day);
            case "moderate rain":
                txtMessage.setText(R.string.moderate_rain);
            case "heavy rain":
                txtMessage.setText(R.string.heavy_rain);
            default:
                txtMessage.setText(R.string.cloudy_day);
        }

        WeatherForecastAdapter adapter = new WeatherForecastAdapter(getContext(), weatherForecastResult);
        rec_forecast.setAdapter(adapter);
        weatherForecastView.setVisibility(View.VISIBLE);
        Log.d("adapter", "reached");

    }

    public void setBackgroundImage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour < 12) {
            time_bg.setImageResource(R.drawable.day_bg);
            ((Home) getActivity()).updateStatusBarColor("#f9c761");
        } else if (hour >= 12 && hour < 17) {
            time_bg.setImageResource(R.drawable.day_bg);
            ((Home) getActivity()).updateStatusBarColor("#f9c761");
        } else if (hour >= 17 && hour < 20) {
            time_bg.setImageResource(R.drawable.night_bg);
            ((Home) getActivity()).updateStatusBarColor("#444eae");
        } else {
            time_bg.setImageResource(R.drawable.night_bg);
            ((Home) getActivity()).updateStatusBarColor("#444eae");

        }

    }

    @Override
    public void onBackPressed() {
        navController.navigate(R.id.action_weatherForecastFragment_to_todayFragment);
        ((Home)getActivity()).hideBottomNavigation(false);
    }

    public static BackpressedListener backpressedlistener;

    @Override
    public void onPause() {
        backpressedlistener = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        backpressedlistener = this;
    }

}