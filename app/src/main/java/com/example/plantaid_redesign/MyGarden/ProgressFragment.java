package com.example.plantaid_redesign.MyGarden;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.plantaid_redesign.Model.ProgressModel;
import com.example.plantaid_redesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProgressFragment extends Fragment {
    private CardView cardView1, cardView2, cardView3, cardView4, cardView5;
    private TextView txtEarlyGrowth, txtVegetative, txtFlowering, txtFruitFormation, txtMatureFruiting;
    private TextView txtNeedHelp, txtNeedHelp0, txtNeedHelp1, txtNeedHelp2, txtNeedHelp5;
    private TextView textView15, textView150, textView151, textView152, textView155;
    private ImageView imgEarlyGrowth, imgVegetative, imgFlowering, imgFruitFormation, imgMatureFruiting;
    private TextView txtPlantAge;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    private String userKey;
    private String plantKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        UserMyGardenPlantsActivity activity = (UserMyGardenPlantsActivity) getActivity();
        plantKey = activity.getPlantKey();
        userKey = activity.getUserKey();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try{

            cardView1 = view.findViewById(R.id.cardView1);
            cardView2 = view.findViewById(R.id.cardView2);
            cardView3 = view.findViewById(R.id.cardView3);
            cardView4 = view.findViewById(R.id.cardView4);
            cardView5 = view.findViewById(R.id.cardView5);

            txtEarlyGrowth = view.findViewById(R.id.txtEarlyGrowth);
            txtVegetative = view.findViewById(R.id.txtVegetative);
            txtFlowering = view.findViewById(R.id.txtFlowering);
            txtFruitFormation = view.findViewById(R.id.txtFruitFormation);
            txtMatureFruiting = view.findViewById(R.id.txtMatureFruiting);

            //need help tips (view is gone by default)
            txtNeedHelp = view.findViewById(R.id.txtNeedHelp);
            txtNeedHelp0 = view.findViewById(R.id.txtNeedHelp0);
            txtNeedHelp1 = view.findViewById(R.id.txtNeedHelp1);
            txtNeedHelp2 = view.findViewById(R.id.txtNeedHelp2);
            txtNeedHelp5 = view.findViewById(R.id.txtNeedHelp5);

            //textview for clicking need help
            textView15 = view.findViewById(R.id.textView15);
            textView150 = view.findViewById(R.id.textView150);
            textView151 = view.findViewById(R.id.textView151);
            textView152 = view.findViewById(R.id.textView152);
            textView155 = view.findViewById(R.id.textView155);

            //imageview for each growth phase
            imgEarlyGrowth = view.findViewById(R.id.imgEarlyGrowth);
            imgVegetative = view.findViewById(R.id.imgVegetative);
            imgFlowering = view.findViewById(R.id.imgFlowering);
            imgFruitFormation = view.findViewById(R.id.imgFruitFormation);
            imgMatureFruiting = view.findViewById(R.id.imgMatureFruiting);

            //Initialize firebase
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();


            DatabaseReference plantRef = database.getReference("Plants").child(plantKey).child("plantGrowth");
            plantRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ProgressModel model = snapshot.getValue(ProgressModel.class);
                    if (model != null){
                        String earlyGrowth = model.getEarlyGrowth();
                        String earlyGrowthHelp = model.getEarlyGrowthHelp();
                        String earlyGrowthPic= model.getEarlyGrowthPic();
                        showCardView1(earlyGrowth, earlyGrowthHelp, earlyGrowthPic);

                        String vegetative = model.getVegatative();
                        String vegetativeHelp = model.getVegatativeHelp();
                        String vegetativePic = model.getVegetativePic();
                        showCardView2(vegetative,vegetativeHelp,vegetativePic);

                        String flowering = model.getFlowering();
                        String floweringHelp = model.getFloweringHelp();
                        String floweringPic = model.getFloweringPic();
                        showCardView3(flowering,floweringHelp,floweringPic);

                        String fruitFormation = model.getFruitFormation();
                        String fruitFormationHelp = model.getFruitFormationHelp();
                        String fruitFormationPic = model.getFruitFormationpic();

                        if(!fruitFormation.equals("")){
                            showCardView4(fruitFormation, fruitFormationHelp, fruitFormationPic);
                        }

                        String matureFruiting = model.getMatureFruiting();
                        String matureFruitingHelp = model.getMatureFruitingHelp();
                        String matureFruitingPic = model.getMatureFruitingPic();

                        if(!matureFruiting.equals("")){
                            showCardView5(matureFruiting, matureFruitingHelp, matureFruitingPic);
                        }

                    }
                    else{

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }catch (Exception e) {
            Log.v("TEST", "Error: " + e.toString());
        }
    }


    public void showCardView1(String growthPhase, String help, String pic){
        cardView1.setVisibility(View.VISIBLE);
        txtEarlyGrowth.setText(growthPhase);
        Picasso.get().load(pic).placeholder(R.drawable.ic_launcher_foreground).into(imgEarlyGrowth);
        txtNeedHelp.setText(help);
        textView15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtNeedHelp.getVisibility() == View.VISIBLE){
                    txtNeedHelp.setVisibility(View.GONE);
                }
                else{
                    txtNeedHelp.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showCardView2(String growthPhase, String help, String pic){
        cardView2.setVisibility(View.VISIBLE);
        txtVegetative.setText(growthPhase);
        txtNeedHelp0.setText(help);
        Picasso.get().load(pic).placeholder(R.drawable.ic_launcher_foreground).into(imgVegetative);
        textView150.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtNeedHelp0.getVisibility() == View.VISIBLE){
                    txtNeedHelp0.setVisibility(View.GONE);
                }
                else{
                    txtNeedHelp0.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showCardView3(String growthPhase, String help, String pic){
        cardView3.setVisibility(View.VISIBLE);
        txtFlowering.setText(growthPhase);
        txtNeedHelp1.setText(help);
        Picasso.get().load(pic).placeholder(R.drawable.ic_launcher_foreground).into(imgFlowering);
        textView151.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtNeedHelp1.getVisibility() == View.VISIBLE){
                    txtNeedHelp1.setVisibility(View.GONE);
                }
                else{
                    txtNeedHelp1.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showCardView4(String growthPhase, String help, String pic){
        cardView4.setVisibility(View.VISIBLE);
        txtFruitFormation.setText(growthPhase);
        txtNeedHelp2.setText(help);
        Picasso.get().load(pic).placeholder(R.drawable.ic_launcher_foreground).into(imgFruitFormation);
        textView152.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtNeedHelp2.getVisibility() == View.VISIBLE){
                    txtNeedHelp2.setVisibility(View.GONE);
                }
                else{
                    txtNeedHelp2.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showCardView5(String growthPhase, String help, String pic){
        cardView5.setVisibility(View.VISIBLE);
        txtMatureFruiting.setText(growthPhase);
        txtNeedHelp5.setText(help);
        Picasso.get().load(pic).placeholder(R.drawable.ic_launcher_foreground).into(imgMatureFruiting);
        textView155.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtNeedHelp5.getVisibility() == View.VISIBLE){
                    txtNeedHelp5.setVisibility(View.GONE);
                }
                else{
                    txtNeedHelp5.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}