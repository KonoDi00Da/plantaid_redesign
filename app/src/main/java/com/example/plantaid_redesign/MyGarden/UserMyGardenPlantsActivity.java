package com.example.plantaid_redesign.MyGarden;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.plantaid_redesign.Adapter.MyGardenTabLayoutAdapter;
import com.example.plantaid_redesign.Adapter.TabLayoutAdapter;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.WeatherForecastFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

public class UserMyGardenPlantsActivity extends AppCompatActivity {
    private static final String TAG = "UserMyGarden";


    private ImageView imageView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private MyGardenTabLayoutAdapter myGardenTabLayoutAdapter;
    private FloatingActionButton btn_back;
    public String plantKey, userKey, commonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_my_garden_plants);
        imageView = findViewById(R.id.imgPlant);
        btn_back = findViewById(R.id.btn_back);


        Intent intent = getIntent();
        plantKey = intent.getStringExtra("plantKey");
        userKey = intent.getStringExtra("userKey");
        commonName = intent.getStringExtra("commonName");

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        String plantCom = commonName;

        Picasso.get().load(getIntent().getStringExtra("plant_image"))
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imageView);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#485E0D"));
            }

            tabLayout = findViewById(R.id.tab_layout);
            viewPager2 = findViewById(R.id.view_pager);

            myGardenTabLayoutAdapter = new MyGardenTabLayoutAdapter(this);
            viewPager2.setAdapter(myGardenTabLayoutAdapter);


            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager2.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    tabLayout.getTabAt(position).select();
                }
            });

            Bundle bundle = new Bundle();
            bundle.putString("plantKey", plantKey);
            bundle.putString("userKey", userKey);
            bundle.putString("commonName", commonName);
            PlantCareFragment fragInfo = new PlantCareFragment();
            fragInfo.setArguments(bundle);


        }catch (Exception e){
            Log.d(TAG, "fix",e);
        }

    }
    public void hideTabLayout(boolean flag){
        if(flag){
            tabLayout.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        } else{
            tabLayout.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }



    public String getPlantKey() {
        return plantKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public String getCommonName() { return commonName; }


}