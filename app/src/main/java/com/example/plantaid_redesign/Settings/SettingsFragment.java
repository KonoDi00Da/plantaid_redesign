package com.example.plantaid_redesign.Settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.plantaid_redesign.LoginRegister.LoginRegisterActivity;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsFragment extends Fragment implements BackpressedListener {
    private NavController navController;
    public static final String TAG = "Settings";
    private Button btnLogout, btnAboutUs, btnHelp, btnPolicy, termsUse;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnAboutUs = view.findViewById(R.id.btnAboutUs);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnPolicy = view.findViewById(R.id.btnPolicy);
        termsUse = view.findViewById(R.id.termsUse);
        //Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        try{
            logoutUser();
            showTerms();
            showAboutUs();
            showHelp();
            showPolicy();

        }catch (Exception e){
            Log.e(TAG, "tab", e);
        }

    }

    private void showPolicy() {
        btnPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Home)getActivity()).hideBottomNavigation(true);
                navController.navigate(R.id.action_settingsFragment_to_privacyPolicyFragment);
            }
        });
    }

    private void showHelp() {
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Home)getActivity()).hideBottomNavigation(true);
                navController.navigate(R.id.action_settingsFragment_to_moreHelpFragment);
            }
        });
    }

    private void showAboutUs() {
        btnAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Home)getActivity()).hideBottomNavigation(true);
                navController.navigate(R.id.action_settingsFragment_to_aboutUsFragment);
            }
        });
    }

    private void showTerms() {
        termsUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Home)getActivity()).hideBottomNavigation(true);
                navController.navigate(R.id.action_settingsFragment_to_termsFragment);
            }
        });
    }

    private void toast(String msg ){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private void logoutUser() {
        try{
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), LoginRegisterActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });

        } catch (Exception e){
            toast("Something went wrong, please try again");
            Log.e("Logout error", "exception", e);
        }
    }

    @Override
    public void onBackPressed() {
        ((Home) getActivity()).home();
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