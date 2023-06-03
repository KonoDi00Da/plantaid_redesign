package com.example.plantaid_redesign.Journal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.plantaid_redesign.Adapter.JournalAdapter;
import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.Model.Journal;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JournalFragment extends Fragment implements JournalAdapter.OnItemClickListener, BackpressedListener {
    private static final String TAG = "Journal";
    private RecyclerView rvJournalList;
    private ImageView imgNoContent;
    private FloatingActionButton fabAddEntry;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private ValueEventListener valueEventListener;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private LoadingDialog dialog;
    private NavController navController;
    private JournalAdapter journalAdapter;
    private List<Journal> journalList;

    private ImageButton btnTrash;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            rvJournalList = view.findViewById(R.id.rvJournalList);
            imgNoContent = view.findViewById(R.id.imgNoContent);
            fabAddEntry = view.findViewById(R.id.fabAdd);
            navController = Navigation.findNavController(view);
            btnTrash = view.findViewById(R.id.btnTrash);

            dialog = new LoadingDialog(getActivity());
            //((HomeActivity)getActivity()).setStatusBar(statusBar);
            //((HomeActivity)getActivity()).setLightStatusBar(true);

            //Initialize firebase
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();
            reference = database.getReference().child("journal").child(currentUser.getUid());

            journalList = new ArrayList<>();
            journalAdapter = new JournalAdapter(journalList);
            rvJournalList.setAdapter(journalAdapter);

            populateTimeline();
            journalAdapter.setOnItemClickListener(JournalFragment.this);
            disableButtons();

            add();

            btnTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.action_journalFragment_to_recentlyDeletedFragment);
                }
            });

        } catch (Exception e){
            Log.e(TAG, "onViewCreated: ", e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }

    private void disableButtons(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            fabAddEntry.setEnabled(false);
        }
    }

    private void add() {
        fabAddEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("mode", "new entry");
                    navController.navigate(R.id.action_journalFragment_to_editEntryFragment, bundle);
                } catch (Exception e){
                    Log.e(TAG, "onClick: ", e);
                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateTimeline() {
        try {
            valueEventListener = new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Journal ev = new Journal();

                        for(DataSnapshot data : snapshot.getChildren()){
                            ev = data.getValue(Journal.class);
                            journalList.add(ev);
                        }

                        Collections.reverse(journalList);
                        journalAdapter.updateDataSet(journalList);

                        if(journalAdapter.getItemCount() < 1){
                            imgNoContent.setVisibility(View.VISIBLE);
                        } else {
                            imgNoContent.setVisibility(View.GONE);
                        }
                    } catch (Exception e){
                        Log.e(TAG, "Message: ", e);
                        Toast.makeText(getActivity(), "Please wait...", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };

            reference.addValueEventListener(valueEventListener);
        } catch (Exception e){
            Log.e(TAG, "populateTimeline: ", e);
            Toast.makeText(getActivity(), "Please wait...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(int position) {
        try {
            if (journalList != null && position < journalList.size()) {
                Journal journal = journalList.get(position);
                Bundle bundle = new Bundle();
                bundle.putString("id", journal.getId());
                navController.navigate(R.id.action_journalFragment_to_viewEntryFragment, bundle);
            }
        } catch (Exception e){
            Log.e(TAG, "onItemClick: ", e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
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