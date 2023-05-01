package com.example.plantaid_redesign.LoginRegister;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.R;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";
    private ImageView checkbox_outline;
    private TextView txtLogin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        checkbox_outline = view.findViewById(R.id.checkbox_outline);
        txtLogin = view.findViewById(R.id.txtLogin);

        try {
            loginFragment();
            acceptTerms();
        }catch (Exception e){
            Log.d(TAG, "fix",e);
        }
    }

    private void loginFragment(){
        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.action_registerFragment_to_loginFragment);
                }catch (Exception e){
                    Log.d(TAG, "login",e);
                }
            }
        });
    }

    private boolean isClicked = true;
    private void acceptTerms(){
        checkbox_outline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(isClicked){
                        checkbox_outline.setBackground(getResources().getDrawable(R.drawable.ic_baseline_check_box_outline_blank_24));
                        isClicked = false;
                    } else {
                        checkbox_outline.setBackground(getResources().getDrawable(R.drawable.ic_baseline_check_box_24));
                        isClicked = true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "toggleCheck: ", e);
                }
            }
        });
    }

    private void toast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}