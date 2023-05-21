package com.example.plantaid_redesign.LoginRegister;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.Adapter.TabLayoutAdapter;
import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.Model.User;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";
    private ImageView checkbox_outline;

    private FirebaseAuth mAuth;
    private EditText txtFirstName, txtLastName,txtEmail, txtPassword, txtConfirmPassword;
    private TextView txtPolicy;
    private LoadingDialog loadingDialog;
    private boolean isClicked = false;
    private Button btnRegister;

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
        txtFirstName = view.findViewById(R.id.txtFirstName);
        txtLastName = view.findViewById(R.id.txtLastName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPassword = view.findViewById(R.id.txtPassword);
        txtConfirmPassword = view.findViewById(R.id.txtConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        txtPolicy = view.findViewById(R.id.txtPolicy);

        loadingDialog = new LoadingDialog(getActivity());
        mAuth = FirebaseAuth.getInstance();


        try {
            registerUser();
            acceptTerms();
            showTerms();
        }catch (Exception e){
            Log.d(TAG, "fix",e);
        }
    }

    private void showTerms(){
        txtPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RegisterTermsUsePolicy.class);
                startActivity(intent);

            }
        });
    }

    private void registerUser() {
       btnRegister.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               try {
                   String email = txtEmail.getText().toString().trim();
                   String password = txtPassword.getText().toString().trim();
                   String confirmPassword = txtConfirmPassword.getText().toString().trim();
                   String firstName = txtFirstName.getText().toString().trim();
                   String lastName = txtLastName.getText().toString().trim();

                   if (firstName.isEmpty()) {
                       txtFirstName.setError("First name is required");
                       txtFirstName.requestFocus();
                       return;
                   }
                   if (lastName.isEmpty()) {
                       txtLastName.setError("Last name is required");
                       txtLastName.requestFocus();
                       return;
                   }
                   if (email.isEmpty()) {
                       txtEmail.setError("Email is required");
                       txtEmail.requestFocus();
                       return;
                   }
                   if (password.isEmpty()) {
                       txtPassword.setError("Password is required");
                       txtPassword.requestFocus();
                       return;
                   }
                   if (confirmPassword.isEmpty()) {
                       txtConfirmPassword.setError("Confirm your password");
                       txtConfirmPassword.requestFocus();
                       return;
                   }
                   if (password.length() < 6) {
                       txtPassword.setError("Password should be at least 6 characters");
                       txtPassword.requestFocus();
                       return;
                   }
                   if (!password.equals(confirmPassword)) {
                       txtConfirmPassword.setError("Password doesn't match");
                       txtConfirmPassword.requestFocus();
                       return;
                   }
                   if (isClicked==false) {
                       toast("Must accept policy and terms");
                       return;
                   }

                   //put data into database
                   loadingDialog.startLoading("Registering your account");
                   mAuth.fetchSignInMethodsForEmail(email)
                           .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                               @Override
                               public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                   boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                                   //Log.e("TAG", "Is New User!");
                                   if (isNewUser) {
                                       mAuth.createUserWithEmailAndPassword(email, password)
                                               .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<AuthResult> task) {
                                                       if (task.isSuccessful()) {
                                                           mAuth.getCurrentUser().sendEmailVerification()
                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                           if (task.isSuccessful()) {
                                                                               User user = new User(firstName, lastName, email);
                                                                               FirebaseDatabase.getInstance().getReference("Users")
                                                                                       .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                                       .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                           @Override
                                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                               loadingDialog.stopLoading();
                                                                                               showDialog();
                                                                                           }
                                                                                       });
                                                                           } else {
                                                                               toast(task.getException().getMessage().toString());
                                                                           }
                                                                       }
                                                                   });
                                                       } else {
                                                           Log.e(TAG, "onComplete: Failed=" + task.getException().getMessage());
                                                           loadingDialog.stopLoading();
                                                           toast("Please try again.");
                                                       }
                                                   }
                                               });
                                   } else {
                                       //Log.e("TAG", "Is Old User!");
                                       loadingDialog.stopLoading();
                                       txtEmail.setError("The email address is already in use by another account.");
                                   }
                               }
                           });
               } catch (Exception e) {
                   toast("Submission Error, Please try again");
                   Log.e("Submit Error", "exception", e);
               }
           }
       });
    }

    private void showDialog() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.cardview_email_verification);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnToLogin = dialog.findViewById(R.id.btnToLogin);

        btnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                loginRegisterActivity.returnLogin();
                Intent intent = new Intent(getActivity(), LoginRegisterActivity.class);
                intent.putExtra("fragment_id", 0);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

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