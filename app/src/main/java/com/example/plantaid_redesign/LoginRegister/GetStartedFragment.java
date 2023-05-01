package com.example.plantaid_redesign.LoginRegister;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.plantaid_redesign.R;
import com.github.hariprasanths.bounceview.BounceView;

public class GetStartedFragment extends Fragment {

    private static final String TAG = "GetStartedFragment";
    private Button btnGetStarted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_get_started, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnGetStarted = view.findViewById(R.id.btnGetStarted);
        BounceView.addAnimTo(btnGetStarted);
        NavController navController = Navigation.findNavController(view);

        try{
            RelativeLayout imageFlower;
            TextView txt1, txt2;
            txt1 = view.findViewById(R.id.txt1);
            txt2 = view.findViewById(R.id.txt2);
            imageFlower = view.findViewById(R.id.imageFlower);
            Animation animFadein, animslideup;

            animFadein = AnimationUtils.loadAnimation(getActivity(),
                    R.anim.fade_in);
            animslideup = AnimationUtils.loadAnimation(getActivity(),
                    R.anim.slide_up);

            final AnimationSet s = new AnimationSet(true);
            s.setInterpolator(new AccelerateInterpolator());

            s.addAnimation(animslideup);
            s.addAnimation(animFadein);
            txt1.startAnimation(s);
            txt2.startAnimation(s);
            btnGetStarted.startAnimation(s);
            imageFlower.startAnimation(s);

            Window window = getActivity().getWindow();
            window.setStatusBarColor(ContextCompat.getColor(getActivity(),R.color.light_green));
            btnGetStarted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navController.navigate(R.id.action_getStartedFragment_to_registerFragment);

                }
            });
        }catch (Exception e){
            Log.e(TAG, "fix",e);
        }
    }
}