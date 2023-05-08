package com.example.plantaid_redesign.Today;

import static androidx.fragment.app.FragmentManager.TAG;

import android.media.Image;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.Common.Common;
import com.example.plantaid_redesign.Model.WeatherResult;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Retrofit.IOpenWeatherMap;
import com.example.plantaid_redesign.Retrofit.RetrofitClient;
import com.example.plantaid_redesign.WeatherForecastFragment;
import com.squareup.picasso.Picasso;

import java.time.LocalTime;
import java.util.Calendar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class TodayFragment extends Fragment {
    private static final String TAG = "home";
    private ImageView btnNavDrawer, imgTimeBg, currentWeatherIcon;
    private CardView weatherForecastCard;
    private TextView txtTemperature, txtLocation, txtCondition, txtDateTime;
    private RelativeLayout weatherForecastView;
    private String txtSunrise, txtSunset, txtGeoCoords;

    private ProgressBar loading;

    private CompositeDisposable compositeDisposable;
    private IOpenWeatherMap mService;

    public TodayFragment(){
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((Home)getActivity()).updateStatusBarColor("#485E0D");
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnNavDrawer = view.findViewById(R.id.btnNavDrawer);
        weatherForecastCard = view.findViewById(R.id.weatherForecastCard);
        imgTimeBg = view.findViewById(R.id.imgTimeBg);
        currentWeatherIcon = view.findViewById(R.id.currentWeatherIcon);
        txtTemperature = view.findViewById(R.id.txtTemperature);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtCondition = view.findViewById(R.id.txtCondition);
        txtDateTime = view.findViewById(R.id.txtDateTime);
        loading = view.findViewById(R.id.progressBar);
        weatherForecastView = view.findViewById(R.id.weatherForecastView);

        try{
            openDrawer();
            openWeatherForecastFragment();
            setBackgroundImage();
            getWeatherInformation();

        }catch (Exception e){
            Log.e(TAG, "tab", e);
        }

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

    public void openWeatherForecastFragment(){
        weatherForecastCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeatherForecastFragment weatherForecastFragment= new WeatherForecastFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.full_body_container, weatherForecastFragment, "findThisFragment")
                        .addToBackStack(null)
                        .commit();
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


}