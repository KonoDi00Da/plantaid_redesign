package com.example.plantaid_redesign.Today;

import static androidx.fragment.app.FragmentManager.TAG;

import android.media.Image;
import android.nfc.Tag;
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
import android.widget.Toast;

import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.WeatherForecastFragment;

public class TodayFragment extends Fragment {
    private static final String TAG = "home";
    private ImageView btnNavDrawer;
    private CardView weatherForecastCard;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnNavDrawer = view.findViewById(R.id.btnNavDrawer);
        weatherForecastCard = view.findViewById(R.id.weatherForecastCard);

        try{
            openDrawer();
            openWeatherForecastFragment();

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

    public void openWeatherForecastFragment(){
        weatherForecastCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeatherForecastFragment weatherForecastFragment= new WeatherForecastFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.body_container, weatherForecastFragment, "findThisFragment")
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

}