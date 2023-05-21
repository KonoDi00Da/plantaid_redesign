package com.example.plantaid_redesign.LoginRegister;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.plantaid_redesign.Adapter.TabLayoutAdapter;
import com.example.plantaid_redesign.R;
import com.google.android.material.tabs.TabLayout;

public class LoginRegisterActivity extends AppCompatActivity {
    private static final String TAG = "LoginRegister";

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private TabLayoutAdapter tabLayoutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#8EB879"));
            }
            setContentView(R.layout.activity_login_register);

            tabLayout = findViewById(R.id.tab_layout);
            viewPager2 = findViewById(R.id.view_pager);

            tabLayoutAdapter = new TabLayoutAdapter(this);
            viewPager2.setAdapter(tabLayoutAdapter);

            Intent bundle = getIntent();
            int id_frag = bundle.getIntExtra("fragment_id",0);
            viewPager2.setCurrentItem(id_frag);

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

        }catch (Exception e){
            Log.d(TAG, "fix",e);
        }

    }

}