package com.example.plantaid_redesign.MyGarden;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.Adapter.ReminderPlant_Adapter;
import com.example.plantaid_redesign.Model.PlantReminderModel;
import com.example.plantaid_redesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlantCareFragment extends Fragment {
    public static final String TAG = "MyGarden";

    private CardView cardCustom, cardWater, cardFertilize, cardRepot;
    private String plantCommonName, userKey, plantKey;
    private TextView txtTest;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;

    private ReminderPlant_Adapter cAdapter;
    private RecyclerView recyclerView;
    ArrayList<PlantReminderModel> list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plant_care, container, false);
        UserMyGardenPlantsActivity activity = (UserMyGardenPlantsActivity) getActivity();
        plantCommonName = activity.getCommonName();
        userKey = activity.getUserKey();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cardCustom = view.findViewById(R.id.cardCustom);
        cardRepot = view.findViewById(R.id.cardRepot);
        cardWater = view.findViewById(R.id.cardWater);
        cardFertilize = view.findViewById(R.id.cardFertilize);
        recyclerView = view.findViewById(R.id.reminderRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setHasFixedSize(true);
        cAdapter = new ReminderPlant_Adapter(list, getActivity());
        recyclerView.setAdapter(cAdapter);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        try {
            showPlantReminders();
            openReminderFrag();
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }


    public void showPlantReminders(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("myGarden").child(userKey)
                .child("plantReminders");
//            databaseReference.child("Users").keepSynced(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        PlantReminderModel plantReminders = dataSnapshot.getValue(PlantReminderModel.class);
                        list.add(plantReminders);
                    }
                    cAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast("Error");

            }
        });
    }
    public void openReminderFrag(){
        Intent intent = new Intent(getActivity(), PlantCare_Add_Reminder.class);
        intent.putExtra("plantCommonName", plantCommonName);
        intent.putExtra("userKey", userKey);

        cardCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("taskReminder", "Custom");
                startActivity(intent);
            }
        });
        cardFertilize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("taskReminder", "Fertilize");
                startActivity(intent);
            }
        });
        cardRepot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("taskReminder", "Repot");
                startActivity(intent);
            }
        });
        cardWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("taskReminder", "Water");
                startActivity(intent);
            }
        });
    }



    private void toast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}