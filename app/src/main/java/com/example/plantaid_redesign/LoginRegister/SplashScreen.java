package com.example.plantaid_redesign.LoginRegister;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.github.hariprasanths.bounceview.BounceView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "GetStartedFragment";
    private static final String CHANNEL_ID = "PlantAidReminder";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        try{
            createNotificationChannel();
            animate();
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if(firebaseUser != null && firebaseUser.isEmailVerified()){
                        Intent intent = new Intent(SplashScreen.this, Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent homeScreen = new Intent(SplashScreen.this, LoginRegisterActivity.class);
                        startActivity(homeScreen);

                        finish();
                    }
                }
            }, 1*2000);
        }catch (Exception e){
            Log.e(TAG, "fix",e);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PlantAidReminderChannel";
            String description = "Channel for PlantAid Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void animate(){
        RelativeLayout imageFlower;
        TextView txt1, txt2;
        txt1 = findViewById(R.id.txt1);
        txt2 = findViewById(R.id.txt2);
        imageFlower = findViewById(R.id.imageFlower);
        Animation animFadein, animslideup;

        animFadein = AnimationUtils.loadAnimation(this,
                R.anim.fade_in);
        animslideup = AnimationUtils.loadAnimation(this,
                R.anim.slide_up);

        final AnimationSet s = new AnimationSet(true);
        s.setInterpolator(new AccelerateInterpolator());

        s.addAnimation(animslideup);
        s.addAnimation(animFadein);
        txt1.startAnimation(s);
        txt2.startAnimation(s);
        imageFlower.startAnimation(s);

        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.light_green));

    }
}