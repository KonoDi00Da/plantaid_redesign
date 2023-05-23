package com.example.plantaid_redesign.MyGarden;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.plantaid_redesign.Adapter.AllPlantListAdapter;
import com.example.plantaid_redesign.Adapter.CurrentPlantsAdapter;
import com.example.plantaid_redesign.Model.AllPlantsList;
import com.example.plantaid_redesign.Model.User_Plants;
import com.example.plantaid_redesign.MyGarden.MyGardenAllPlantsFragment;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class  MyGardenFragment extends Fragment implements BackpressedListener{
    private static final String TAG = "MyGarden";
    private NavController navController;
    RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private ArrayList<User_Plants> list = new ArrayList<>();
    private CardView cardNoPlants;




    private ImageView btnAddPlant, btnNavDrawer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((Home)getActivity()).updateStatusBarColor("#485E0D");
        return inflater.inflate(R.layout.fragment_my_garden, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        btnAddPlant = view.findViewById(R.id.btnAddPlant);
        btnNavDrawer = view.findViewById(R.id.btnNavDrawer);
        cardNoPlants = view.findViewById(R.id.cardNoPlants);
        recyclerView = view.findViewById(R.id.mRecyclerView);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        list = new ArrayList<>();

        try{
            openDrawer();
            addPlant();
            showAllPlants();

        }catch (Exception e){
            Log.e(TAG, "tab", e);
        }

    }
    public void showAllPlants(){
        CurrentPlantsAdapter recyclerAdapter = new CurrentPlantsAdapter(list,getContext(),navController);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), 0));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(recyclerAdapter);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("MyGarden").child(currentUser.getUid());
        //databaseReference.child("Users").keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    cardNoPlants.setVisibility(View.GONE);//noplants
                    User_Plants userPlants = new User_Plants();

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        userPlants = dataSnapshot.getValue(User_Plants.class);
                        list.add(userPlants);
                    }

                    recyclerAdapter.notifyDataSetChanged();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast("Error");

            }

        });
    }

    private void addPlant() {
        btnAddPlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Home)getActivity()).hideBottomNavigation(true);
                navController.navigate(R.id.action_myGardenFragment_to_myGardenAllPlantsFragment);
            }
        });
    }

    public void openDrawer(){
        btnNavDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle result = new Bundle();
                result.putString("bundleKey", "clicked");
                // The child fragment needs to still set the result on its parent fragment manager
                getParentFragmentManager().setFragmentResult("requestKey", result);

            }
        });
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

    @Override
    public void onBackPressed() {
        ((Home)getActivity()).home();
    }

    private void toast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}