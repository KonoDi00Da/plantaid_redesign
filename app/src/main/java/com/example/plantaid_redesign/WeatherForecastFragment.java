package com.example.plantaid_redesign;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Today.TodayFragment;

import java.time.LocalTime;
import java.util.Calendar;

public class WeatherForecastFragment extends Fragment {
    private ImageView btn_back, time_bg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_forecast, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_back = view.findViewById(R.id.btn_back);
        time_bg = view.findViewById(R.id.time_bg);

        try {
            setBackgroundImage();
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Home) getActivity()).updateStatusBarColor("#485E0D");
                    getActivity().onBackPressed();
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "tab", e);
        }


    }

    public void setBackgroundImage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour < 12) {
            time_bg.setImageResource(R.drawable.day_bg);
        } else if (hour >= 12 && hour < 17) {
            time_bg.setImageResource(R.drawable.day_bg);
        } else if (hour >= 17 && hour < 20) {
            time_bg.setImageResource(R.drawable.day_bg);
        } else {
            time_bg.setImageResource(R.drawable.night_bg);
        }

    }
}