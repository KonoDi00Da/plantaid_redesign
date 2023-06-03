package com.example.plantaid_redesign.Journal;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.TransitionManager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantaid_redesign.Adapter.TrashAdapter;
import com.example.plantaid_redesign.Common.LoadingDialog;
import com.example.plantaid_redesign.Model.Journal;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.example.plantaid_redesign.Utilities.BackpressedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentlyDeletedFragment extends Fragment implements TrashAdapter.AdapterListener, TrashAdapter.OnItemCheckListener, BackpressedListener {
    private static final String TAG = "Journal";

    private boolean isViewInflating = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;

        try {
            rootView = inflater.inflate(R.layout.fragment_recently_deleted, container, false);
        } catch (InflateException e) {
            // Display an error toast
            //Toast.makeText(getActivity(), "Error inflating the layout", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onCreateView: ", e);
//            handleInflationError();
        }

        return rootView;
    }
    private void handleInflationError() {
        // Notify the user about the error
//        ((Home) getActivity()).noInternetConnection();
        //Toast.makeText(getActivity(), "Error inflating the layout. Please try again.", Toast.LENGTH_SHORT).show();
    }


    private ImageButton btnBack, btnSelect;
    private RecyclerView rvRecentTrash;
    private ImageView imgNoContent;
    private RelativeLayout parent;

    private ImageView bgActions;
    private ExtendedFloatingActionButton fabActions;
    private FloatingActionButton fabRestore, fabDelete;
    private TextView txtTake, txtOpen;

    private boolean isAllFabsVisible, isSelectAll, isCheckBoxVisible;
    private LoadingDialog dialog;
    private NavController navController;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference referenceTrash, referenceJournal;
    private ValueEventListener valueEventListener;

    private TrashAdapter trashAdapter;
    private List<Journal> trashList;
    private List<Journal> currentSelectedItems = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            navController = Navigation.findNavController(view);
            dialog = new LoadingDialog(getActivity());

            parent = view.findViewById(R.id.parent);

            btnBack = view.findViewById(R.id.btnBack);
            btnSelect = view.findViewById(R.id.btnSelect);

            rvRecentTrash = view.findViewById(R.id.rvRecentTrash);
            imgNoContent = view.findViewById(R.id.imgNoContent);

            bgActions = view.findViewById(R.id.bg_actions);
            fabActions = view.findViewById(R.id.fabActions);
            fabRestore = view.findViewById(R.id.fabRestore);
            fabDelete = view.findViewById(R.id.fabDelete);
            txtOpen = view.findViewById(R.id.txtOpen);
            txtTake = view.findViewById(R.id.txtTake);

            //Initialize firebase
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();
            referenceTrash = database.getReference().child("trash").child(currentUser.getUid());
            referenceJournal = database.getReference().child("journal").child(currentUser.getUid());

            trashList = new ArrayList<>();
            trashAdapter = new TrashAdapter(trashList, this, this, getActivity());
            rvRecentTrash.setAdapter(trashAdapter);

            populateTimeline();

            buttonsOnClick();

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onViewCreated: ", e);
        }

    }

    private void buttonsOnClick() {
        isSelectAll = false;
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSelectAll){
                    fabActions.shrink();
                    fabActions.show();
                    trashAdapter.setCheckBoxVisibility(true);
                    isSelectAll = true;
                } else {
                    trashAdapter.setCheckBoxVisibility(false);
                    fabActions.hide();
                    fabActions.shrink();
                    fabRestore.hide();
                    fabDelete.hide();
                    txtTake.setVisibility(View.GONE);
                    txtOpen.setVisibility(View.GONE);
                    bgActions.setVisibility(View.GONE);
                    isSelectAll = false;
                    isAllFabsVisible = false;
                    currentSelectedItems.clear();
                }

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_recentlyDeletedFragment_to_journalFragment, null);
            }
        });

        isAllFabsVisible = false;
        fabActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(parent);
                if (!isAllFabsVisible) {
                    // when isAllFabsVisible becomes true make all the action name texts and FABs VISIBLE.
                    fabRestore.show();
                    fabDelete.show();
                    txtTake.setVisibility(View.VISIBLE);
                    txtOpen.setVisibility(View.VISIBLE);
                    bgActions.setVisibility(View.VISIBLE);
                    // Now extend the parent FAB, as user clicks on the shrinked parent FAB
                    fabActions.extend();
                    // make the boolean variable true as we have set the sub FABs visibility to GONE
                    isAllFabsVisible = true;
                } else {
                    // when isAllFabsVisible becomes true make all the action name texts and FABs GONE.
                    fabRestore.hide();
                    fabDelete.hide();
                    txtTake.setVisibility(View.GONE);
                    txtOpen.setVisibility(View.GONE);
                    bgActions.setVisibility(View.GONE);
                    // Set the FAB to shrink after user closes all the sub FABs
                    fabActions.shrink();
                    // make the boolean variable false as we have set the sub FABs visibility to GONE
                    isAllFabsVisible = false;
                }
            }
        });

        fabRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restore("restore");
            }
        });

        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePermanently("delete");
            }
        });
    }

    private int itemsRestored = 0;
    private void restore(String action) {
        try {
            if(action.equals("restore")){
                dialog.startLoading("Restoring items...");
            }
            int totalItemsToRestore = currentSelectedItems.size();
            for(Journal journal : currentSelectedItems){

                referenceJournal.child(journal.getId()).setValue(journal).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        itemsRestored++;

                        if (itemsRestored == totalItemsToRestore) {
                            deletePermanently(action);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.stopLoading();
                        Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                        Log.e("Move to trash", "Error ", e);
                    }
                });
            }
        } catch (Exception e){
            Log.e(TAG, "moveEntryToTrash: ", e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private int deletedItemCount = 0;
    private void deletePermanently(String action) {
        try {
            if(action.equals("delete")){
                dialog.startLoading("Deleting items...");
            }
            int totalItemsToDelete = currentSelectedItems.size();


            for(Journal journal : currentSelectedItems){
                referenceTrash.child(journal.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        deletedItemCount++;

                        if (deletedItemCount == totalItemsToDelete) {
                            trashList.clear();
                            populateTimeline();
                            dialog.stopLoading();
                            currentSelectedItems.clear();

                            if(action.equals("delete")){
                                Toast.makeText(getActivity(), "Items deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Items restored", Toast.LENGTH_SHORT).show();
                            }

                            trashAdapter.setCheckBoxVisibility(false);
                            fabActions.hide();
                            fabActions.shrink();
                            fabRestore.hide();
                            fabDelete.hide();
                            txtTake.setVisibility(View.GONE);
                            txtOpen.setVisibility(View.GONE);
                            bgActions.setVisibility(View.GONE);
                            isSelectAll = false;
                            isAllFabsVisible = false;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.stopLoading();
                        Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                        Log.e("Move to trash", "Error ", e);
                    }
                });
            }
        } catch (Exception e){
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "deleteEntryInJournal: ", e);
        }
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
                            trashList.add(ev);
                        }

                        Collections.reverse(trashList);
                        trashAdapter.updateDataSet(trashList);

                        if(trashAdapter.getItemCount() < 1){
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

            referenceTrash.addValueEventListener(valueEventListener);
        } catch (Exception e){
            Log.e(TAG, "populateTimeline: ", e);
            Toast.makeText(getActivity(), "Please wait...", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onItemCountChanged() {
        // Update the fragment's UI with the new dataset
        trashList.clear();
        populateTimeline();
    }

    @Override
    public void onItemCheck(Journal item) {
        currentSelectedItems.add(item);
    }

    @Override
    public void onItemUncheck(Journal item) {
        currentSelectedItems.remove(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentSelectedItems.clear();
    }

    public static BackpressedListener backpressedlistener;

    @Override
    public void onBackPressed() {
        if(navController != null){
            navController.navigate(R.id.action_recentlyDeletedFragment_to_journalFragment, null);
        } else {
//            ((Home) getActivity()).goBackMoments();
//            ((Home) getActivity()).dismissNoInternet();
        }
    }

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