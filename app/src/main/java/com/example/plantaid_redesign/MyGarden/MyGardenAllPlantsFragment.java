package com.example.plantaid_redesign.MyGarden;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.plantaid_redesign.Adapter.AllPlantListAdapter;
import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.Model.AllPlantsList;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyGardenAllPlantsFragment extends Fragment implements  BackpressedListener{
    public static final String TAG = "MyGarden";

    private ImageView btnBack;
    private NavController navController;
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    ArrayList<AllPlantsList> plantArrayList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_garden_all_plants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnBack = view.findViewById(R.id.btnBack);
        navController = Navigation.findNavController(view);
        recyclerView = view.findViewById(R.id.mRecyclerView);


        plantArrayList = new ArrayList<>();

        try {
            showAllPlants();
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.action_myGardenAllPlantsFragment_to_myGardenFragment);
                    ((Home)getActivity()).hideBottomNavigation(false);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }

    }
    public void showAllPlants(){
        AllPlantListAdapter recyclerAdapter = new AllPlantListAdapter(plantArrayList,getContext(),navController);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), 0));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(recyclerAdapter);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Plants");
        databaseReference.child("Plants").keepSynced(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    AllPlantsList plantListModel = dataSnapshot.getValue(AllPlantsList.class);
                    plantArrayList.add(plantListModel);
                }
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        navController.navigate(R.id.action_myGardenAllPlantsFragment_to_myGardenFragment);
        ((Home)getActivity()).hideBottomNavigation(false);
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