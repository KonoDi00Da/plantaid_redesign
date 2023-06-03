package com.example.plantaid_redesign.Journal;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.Model.Journal;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewEntryFragment extends Fragment implements BackpressedListener {
    private static final String TAG = "Journal";
    private TextView txtTitle, txtContent;
    private ImageView imgContent;
    private ImageButton btnBack, btnTrash;
    private Button btnEdit;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference reference, referenceTrash;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private LoadingDialog dialog;
    private NavController navController;
    private NavOptions navOptions;
    private RelativeLayout statusBar;

    private String id = "";
    private String photoLink = "";
    private String title = "";
    private String content = "";
    private String time = "";
    private String date = "";

    private Journal journalValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            txtTitle = view.findViewById(R.id.txtTitle);
            txtContent = view.findViewById(R.id.txtContent);
            imgContent = view.findViewById(R.id.imgJournalContent);
            btnBack = view.findViewById(R.id.btnBack);
            btnEdit = view.findViewById(R.id.btnEdit);
            btnTrash = view.findViewById(R.id.btnTrash);
            dialog = new LoadingDialog(getActivity());
            navController = Navigation.findNavController(view);

            statusBar = view.findViewById(R.id.statusBar);

            //Initialize firebase
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();
            reference = database.getReference().child("journal").child(currentUser.getUid());
            referenceTrash = database.getReference().child("trash").child(currentUser.getUid());

            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference().child(currentUser.getUid()).child("images");



            id = getArguments().getString("id");
            //Toast.makeText(getActivity(), "Hello", Toast.LENGTH_SHORT).show();
            showEntry(id);
            back();
            edit();
            zoomImage();
            disableButtons();

        } catch (Exception e){
            Log.e(TAG, "onViewCreated: ", e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void zoomImage() {
        try {
            btnTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialAlertDialogBuilder(getActivity())
                            .setTitle("Move to trash folder?")
                            .setMessage("Are you sure you want to move this entry to trash folder? You can still restore it anytime.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Handle Yes button click
                                    moveEntryToTrash();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                }
            });

        } catch (Exception e){
            Log.e(TAG, "zoomImage: ", e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveEntryToTrash() {
        try {
            dialog.startLoading("Moving to trash...");
            referenceTrash.child(id).setValue(journalValue).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // Move completed successfully
                    deleteEntryInJournal(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                    dialog.stopLoading();
                    Log.e("Move to trash", "Error ", e);
                }
            });
        } catch (Exception e){
            Log.e(TAG, "moveEntryToTrash: ", e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteEntryInJournal(boolean isMoveToTrash) {
        try {
            DatabaseReference reference1 = reference.child(id);
            reference1.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    dialog.stopLoading();
                    if (isMoveToTrash) {
                        Toast.makeText(getActivity(), "Move completed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Entry deleted permanently", Toast.LENGTH_SHORT).show();
                    }
                    navBack();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                    dialog.stopLoading();
                    Log.e("Delete value", "Error ", e);
                }
            });
        } catch (Exception e){
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "deleteEntryInJournal: ", e);
        }
    }


    private void edit() {
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", id);
                    bundle.putString("mode", "update entry");
                    navController.navigate(R.id.action_viewEntryFragment_to_editEntryFragment, bundle);
                } catch (Exception e){
                    Log.e(TAG, "onClick: ", e);
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navBack(){
        try {
            navController.navigate(R.id.action_viewEntryFragment_to_journalFragment, null, navOptions);
        } catch (Exception e){
            Log.e(TAG, "onClick: ", e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void back() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navBack();
            }
        });
    }

    private void showEntry(String id) {
        try {
            DatabaseReference reference1 = reference.child(id);
            reference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Journal journal = dataSnapshot.getValue(Journal.class);
                    if (journal != null){
                        journalValue = journal;
                        photoLink = journal.photo;
                        title = journal.title;
                        content = journal.content;
                        time = journal.time;
                        date = journal.date;

                        txtTitle.setText(title);
                        txtContent.setText(content);

                        if(getActivity() != null){
                            Glide.with(getActivity()).load(photoLink).into(imgContent);

                            Bundle arguments = getArguments();
                            boolean fromAdd;
                            String msg = "";
                            if (arguments != null) {
                                fromAdd = arguments.getBoolean("save_entry");
                                msg = arguments.getString("user_msg");


                                if(fromAdd){
                                    if(msg != null){
//                                        ((Home) getActivity()).sendMessage(msg);
                                    }
                                    //Toast.makeText(getActivity(), "User Message: " + content, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){
            Log.e(TAG, "showEntry: ", e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableButtons(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            btnEdit.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        navBack();
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