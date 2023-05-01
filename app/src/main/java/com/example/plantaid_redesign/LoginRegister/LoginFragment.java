package com.example.plantaid_redesign.LoginRegister;

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
import android.widget.TextView;

import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";

    private TextView txtRegister;
    private Button btnLogin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtRegister = view.findViewById(R.id.txtRegister);
        btnLogin = view.findViewById(R.id.btnLogin);
        try {
            login();
            registerFragment();

        }catch (Exception e){
            Log.d(TAG, "fix",e);
        }
    }

    private void login(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Home.class);
                startActivity(intent);
            }
        });
    }

    private void registerFragment() {
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.action_loginFragment_to_registerFragment);
                }catch (Exception e){
                    Log.d(TAG, "register",e);
                }
            }
        });
    }

    private void forgotPass() {
    }
}