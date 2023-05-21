package com.example.plantaid_redesign.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantaid_redesign.Model.PlantIdentifiedModel;
import com.example.plantaid_redesign.R;
import com.example.plantaid_redesign.Today.Home;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class IdentifyHistoryAdapter extends RecyclerView.Adapter<IdentifyHistoryAdapter.ViewHolder>{
    ArrayList<PlantIdentifiedModel> resultsList;
    private Context context;
    NavController navController;

    public IdentifyHistoryAdapter(ArrayList<PlantIdentifiedModel> resultsList, Context context, NavController navController) {
        this.resultsList = resultsList;
        this.context = context;
        this.navController = navController;
    }

    @NonNull
    @Override
    public IdentifyHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_identified_plants,parent,false);
        return new IdentifyHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlantIdentifiedModel model = resultsList.get(position);
        String commonName;
        String comNameFormat;
        if(model.getCommonName().isEmpty()){
            commonName = "No available record";
        }else{
            commonName = model.getCommonName();
        }
        comNameFormat = commonName.replace(", ","\n");

        holder.txtCommonName.setText(comNameFormat);
        holder.txtSciPlantName.setText(model.getSciName());
        holder.txtScore.setText(model.getScore());
        Picasso.get().load(model.getIdImage()).placeholder(R.mipmap.plantaid_logo).into(holder.imgPlant);
        //this
        Bundle bundle = new Bundle();
        bundle.putString("txtScore", model.getScore());
        bundle.putString("txtScientificName", model.getSciName());
        bundle.putString("txtCommonName", comNameFormat);
        bundle.putString("txtGenus", model.getGenus());
        bundle.putString("txtFamily", model.getFamily());
        bundle.putString("img1", model.getImg1());
        bundle.putString("img2", model.getImg2());
        bundle.putString("img3", model.getImg3());
        bundle.putString("img4", model.getImg4());
        bundle.putString("img5", model.getImg5());


        holder.btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_identifyHistoryFragment3_to_identifyMoreInfoFragment_Identified2, bundle);
            }
        });

    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtScore, txtCommonName, txtSciPlantName;
        ImageView imgPlant;
        Button btnInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPlant = itemView.findViewById(R.id.imgPlant);
            txtCommonName = itemView.findViewById(R.id.txtComPlantName);
            txtSciPlantName = itemView.findViewById(R.id.txtSciPlantName);
            txtScore = itemView.findViewById(R.id.txtScore);
            btnInfo = itemView.findViewById(R.id.btnInfo);

        }
    }
}

