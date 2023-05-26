package com.example.plantaid_redesign.Today;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.plantaid_redesign.Common.Common;
import com.example.plantaid_redesign.Identify.IdentifyContainerFragment;
import com.example.plantaid_redesign.Identify.IdentifyFragment;
import com.example.plantaid_redesign.Identify.IdentifyHistoryFragment;
import com.example.plantaid_redesign.Identify.IdentifyMoreInfoFragment;
import com.example.plantaid_redesign.Identify.IdentifyResultsFragment;
import com.example.plantaid_redesign.MyGarden.MyGardenAllPlantsFragment;
import com.example.plantaid_redesign.MyGarden.MyGardenContainerFragment;
import com.example.plantaid_redesign.MyGarden.MyGardenFragment;
import com.example.plantaid_redesign.MyGarden.MyGardenPlantsDetails;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Settings.AboutUsFragment;
import com.example.plantaid_redesign.Settings.MoreHelpFragment;
import com.example.plantaid_redesign.Settings.PrivacyPolicyFragment;
import com.example.plantaid_redesign.Settings.SettingsContainerFragment;
import com.example.plantaid_redesign.Settings.SettingsFragment;
import com.example.plantaid_redesign.Settings.TermsFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class Home extends AppCompatActivity {
    private static final String TAG = "home";
    private MeowBottomNavigation bottomNav;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottomNav);
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawerLayout);

        try {

//            navigationView.getMenu().findItem(R.id.menuAccount).setOnMenuItemClickListener(menuItem -> {
//                logout();
//                return true;
//            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#485E0D"));
            }

            setBottomNav();

            //data passed from the child fragment (Opening navdrawer)
            getSupportFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                    String doAction = result.getString("bundleKey");
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });

            //Request Permission
            Dexter.withContext(this)
                    .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                buildLocationRequest();
                                buildLocationCallback();

                                if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                    return;
                                }
                                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Home.this);
                                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                            }

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            Toast.makeText(Home.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }).check();

        } catch (Exception e) {
            Log.e(TAG, "bottomnav", e);
        }
    }

    public void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                home();
                Common.current_location = locationResult.getLastLocation();
                Log.d("Location",locationResult.getLastLocation().getLatitude()+"/"+locationResult.getLastLocation().getLongitude());
            }
        };
    }



    public void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10.0f);

    }

    private void setBottomNav(){

        bottomNav.add(new MeowBottomNavigation.Model(1, R.drawable.ic_baseline_local_florist_24));
        bottomNav.add(new MeowBottomNavigation.Model(2, R.drawable.ic_baseline_home_24));
        bottomNav.add(new MeowBottomNavigation.Model(3, R.drawable.ic_baseline_camera_24));
        bottomNav.add(new MeowBottomNavigation.Model(4, R.drawable.ic_baseline_settings_24));
        bottomNav.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
                Fragment fragment;
                Handler handler = new Handler();
                Bundle bundle = new Bundle();
                //bundle.putString("details", "calendar");
                if(item.getId() == 3){
                    fragment = new IdentifyContainerFragment();//this
                }else if(item.getId() == 2){
                    fragment = new TodayContainerFragment();
                }else if(item.getId() == 1){
                    fragment = new MyGardenContainerFragment();
                }else{
                    fragment = new SettingsContainerFragment();
                }

                //avoids lag
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadFragment(fragment);
                    }
                },300);
            }
        });

        Intent bundle = getIntent();
        int id_frag = bundle.getIntExtra("fragment_id",2);
        bottomNav.show(id_frag, true);

        bottomNav.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {

            }
        });

        bottomNav.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {

            }
        });
    }

    public void loadFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.body_container, fragment, null)
                .commit();
    }

    public void updateStatusBarColor(String color){// Color must be in hexadecimal format
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
        }
    }

    public void hideBottomNavigation(boolean flag){
        if(flag){
            bottomNav.setVisibility(View.GONE);
        } else{
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {

        if(MyGardenFragment.backpressedlistener !=null){
            MyGardenFragment.backpressedlistener.onBackPressed();

        } else if(WeatherForecastFragment.backpressedlistener !=null){
            WeatherForecastFragment.backpressedlistener.onBackPressed();
        } else if(MyGardenAllPlantsFragment.backpressedlistener !=null){
            MyGardenAllPlantsFragment.backpressedlistener.onBackPressed();
        } else if(MyGardenPlantsDetails.backpressedlistener !=null){
            MyGardenPlantsDetails.backpressedlistener.onBackPressed();
        } else if(IdentifyFragment.backpressedlistener !=null){
            IdentifyFragment.backpressedlistener.onBackPressed();
        } else if(IdentifyResultsFragment.backpressedlistener !=null){
            IdentifyResultsFragment.backpressedlistener.onBackPressed();
        } else if(IdentifyMoreInfoFragment.backpressedlistener !=null){
            IdentifyMoreInfoFragment.backpressedlistener.onBackPressed();
        } else if(IdentifyHistoryFragment.backpressedlistener !=null){
            IdentifyHistoryFragment.backpressedlistener.onBackPressed();
        } else if(SettingsFragment.backpressedlistener !=null){
            SettingsFragment.backpressedlistener.onBackPressed();
        } else if(TermsFragment.backpressedlistener !=null){
            TermsFragment.backpressedlistener.onBackPressed();
        } else if(AboutUsFragment.backpressedlistener !=null){
            AboutUsFragment.backpressedlistener.onBackPressed();
        } else if(PrivacyPolicyFragment.backpressedlistener !=null){
            PrivacyPolicyFragment.backpressedlistener.onBackPressed();
        } else if(MoreHelpFragment.backpressedlistener !=null){
            MoreHelpFragment.backpressedlistener.onBackPressed();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    public void home(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.body_container, new TodayContainerFragment())
                .commit();
        bottomNav.show(2, true);
    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}