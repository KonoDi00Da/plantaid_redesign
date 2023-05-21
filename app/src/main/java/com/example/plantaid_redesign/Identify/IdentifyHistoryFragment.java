package com.example.plantaid_redesign.Identify;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.plantaid_redesign.Adapter.IdentifyHistoryAdapter;
import com.example.plantaid_redesign.Adapter.PlantIdResultsAdapter;
import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.Model.PlantIdentifiedModel;
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

import java.util.ArrayList;

public class IdentifyHistoryFragment extends Fragment implements BackpressedListener {
    public static final String TAG = "Identify";
    private NavController navController;
    private FloatingActionButton btn_back;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference userRef;
    LoadingDialog loadingDialog;
    ArrayList<PlantIdentifiedModel> list;
    private IdentifyHistoryAdapter cAdapter;
    String u_plantID, plantIdentification, userPlantImage,history, pushKey;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_identify_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        btn_back = view.findViewById(R.id.btn_back);
        list = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        recyclerView = view.findViewById(R.id.identifyResults);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        cAdapter = new IdentifyHistoryAdapter(list, getActivity(), navController);
        recyclerView.setAdapter(cAdapter);

        try {
            showHistory();
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }

    private void showHistory(){
        plantIdentification = "plantIdentification";
        history = "plantIdentificationHistory";
        databaseReference = database.getReference("Users").child(currentUser.getUid()).child(plantIdentification).child(history);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    PlantIdentifiedModel model = dataSnapshot.getValue(PlantIdentifiedModel.class);
                    list.add(model);
                }
                cAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        ((Home)getActivity()).hideBottomNavigation(false);
        navController.navigate(R.id.action_identifyHistoryFragment3_to_identifyFragment);
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

    private void toast(String msg ){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}