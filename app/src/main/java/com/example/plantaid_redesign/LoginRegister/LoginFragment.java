package com.example.plantaid_redesign.LoginRegister;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.github.hariprasanths.bounceview.BounceView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";

    private EditText txtEmail, txtPassword;
    private TextView txtForgotPass;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    LoadingDialog loadingDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogin = view.findViewById(R.id.btnLogin);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPassword = view.findViewById(R.id.txtPassword);
        txtForgotPass = view.findViewById(R.id.txtForgotPass);

        loadingDialog = new LoadingDialog(getActivity());
        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        try {
            login();
            forgotPass();

        }catch (Exception e){
            Log.d(TAG, "fix",e);
        }
    }



    private void login(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String email = txtEmail.getText().toString().trim();
                    String password = txtPassword.getText().toString().trim();

                    if(email.isEmpty()){
                        txtEmail.setError("Email is required");
                        txtEmail.requestFocus();
                        return;
                    }

                    if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        txtEmail.setError("Please enter a valid email");
                        txtEmail.requestFocus();
                        return;
                    }

                    if(password.isEmpty()){
                        txtPassword.setError("Password is required");
                        txtPassword.requestFocus();
                        return;
                    }

                    if(password.length() < 6){
                        txtPassword.setError("Password should be at least 6 characters");
                        txtPassword.requestFocus();
                        return;
                    }

                    loadingDialog.startLoading("Logging in");
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        if(mAuth.getCurrentUser().isEmailVerified()){
                                            loadingDialog.stopLoading();
                                            Intent intent = new Intent(getActivity(), Home.class);
                                            startActivity(intent);
                                            getActivity().finish();
                                        } else {
                                            loadingDialog.stopLoading();
                                            toast("Please verify your email address");
                                        }
                                    } else {
                                        Log.e(TAG, "onComplete: Failed=" + task.getException().getMessage());
                                        loadingDialog.stopLoading();
                                        if (Objects.equals(task.getException().getMessage(), "There is no user record corresponding to this identifier. The user may have been deleted.")){
                                            toast("Email does not exist");
                                        } else if(Objects.equals(task.getException().getMessage(), "The password is invalid or the user does not have a password.")){
                                            toast("Wrong password");
                                        } else if(Objects.equals(task.getException().getMessage(),
                                                "We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]")){
                                            toast("Multiple attempts, please try again later");
                                        }
                                    }
                                }
                            });

                } catch (Exception e){
                    loadingDialog.stopLoading();
                    toast("Login Error, Please try again");
                    Log.e("Login Error", "exception", e);
                }
//                Intent intent = new Intent(getActivity(), Home.class);
//                startActivity(intent);
            }
        });
    }

    private void forgotPass() {
        txtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }

    private void openDialog() {
        try{
            Dialog dialog = new Dialog(getActivity());
            //We have added a title in the custom layout. So let's disable the default title.
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
            dialog.setCancelable(true);
            //Mention the name of the layout of your custom dialog.
            dialog.setContentView(R.layout.reset_password);
            //set size to real size lol
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            EditText resetEmail = dialog.findViewById(R.id.txtEmailReset);
            Button buttonSend = dialog.findViewById(R.id.btnSend);

            resetEmail.setText(resetEmail.getText().toString());
            buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (resetEmail.getText().toString().isEmpty()){
                        resetEmail.setError("Required");
                    } else {
                        dialog.dismiss();
                        loadingDialog.startLoading("Please wait");
                        mAuth.sendPasswordResetEmail(resetEmail.getText().toString()).
                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            loadingDialog.stopLoading();
                                            toast("Please check your mail box");
                                        } else {
                                            toast("Invalid Email");
                                            loadingDialog.stopLoading();
                                            dialog.show();
                                            txtEmail.setText(resetEmail.getText().toString());
                                        }
                                    }
                                });
                    }
                }
            });
            BounceView.addAnimTo(dialog);
            dialog.show();
        } catch (Exception e){
            toast("Something went wrong, Please try again");
            Log.e("Forgot Pass Error", "exception", e);
        }
    }

    private void toast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}