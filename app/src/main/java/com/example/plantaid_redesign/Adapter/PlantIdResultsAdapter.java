package com.example.plantaid_redesign.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantaid_redesign.Identify.IdentifyMoreInfoFragment;
import com.example.plantaid_redesign.Model.PlantIdentifyModel;
import com.example.plantaid_redesign.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PlantIdResultsAdapter extends RecyclerView.Adapter<PlantIdResultsAdapter.ViewHolder>{
    ArrayList<PlantIdentifyModel> resultsList;
    private Context context;
    NavController navController;

    public PlantIdResultsAdapter(ArrayList<PlantIdentifyModel> resultsList, Context context, NavController navController) {
        this.resultsList = resultsList;
        this.context = context;
        this.navController = navController;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_identify_results,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlantIdentifyModel model = resultsList.get(position);
        String commonName;
        String comNameFormat;
        if(model.getComName().isEmpty()){
            commonName = "No available record";
        }else{
            commonName = model.getComName();
        }
        comNameFormat = commonName.replace(", ","\n");
        String score = resultsList.get(position).getScore();
        Double score_ = Double.parseDouble(score) * 100;
        DecimalFormat df = new DecimalFormat("0");
        String newScore = df.format(score_) + "%";
        holder.txtScientificName.setText(resultsList.get(position).getSciName());
        holder.txtCommonName.setText(commonName);
        holder.txtScore.setText(newScore);
        Picasso.get().load(resultsList.get(position).getImg1()).placeholder(R.mipmap.plantaid_logo).into(holder.imgPlant);
//        Picasso.get().load(resultsList.get(position).getImg2()).placeholder(R.drawable.plantaid_logo).into(holder.img2);
//        Picasso.get().load(resultsList.get(position).getImg3()).placeholder(R.drawable.plantaid_logo).into(holder.img3);
//        Picasso.get().load(resultsList.get(position).getImg4()).placeholder(R.drawable.plantaid_logo).into(holder.img4);
//        Picasso.get().load(resultsList.get(position).getImg5()).placeholder(R.drawable.plantaid_logo).into(holder.img5);
        Bundle bundle = new Bundle();
        bundle.putString("txtScore", newScore);
        bundle.putString("txtScientificName", resultsList.get(position).getSciName());
        bundle.putString("txtCommonName", comNameFormat);
        bundle.putString("txtGenus", resultsList.get(position).getGenus());
        bundle.putString("txtFamily", resultsList.get(position).getFamily());
        bundle.putString("img1", resultsList.get(position).getImg1());
        bundle.putString("img2", resultsList.get(position).getImg2());
        bundle.putString("img3", resultsList.get(position).getImg3());
        bundle.putString("img4", resultsList.get(position).getImg4());
        bundle.putString("img5", resultsList.get(position).getImg5());


        holder.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        holder.btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_identifyResultsFragment_to_identifyMoreInfoFragment, bundle);
                }

            });
    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtScientificName, txtCommonName, txtFamily, txtScore;
        ImageView imgPlant, img2, img3, img4, img5;
        Button btnConfirm, btnInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtScientificName = itemView.findViewById(R.id.txtSciPlantName);
            txtCommonName = itemView.findViewById(R.id.txtComPlantName);
            txtScore = itemView.findViewById(R.id.txtScore);
            imgPlant = itemView.findViewById(R.id.imgPlant);
//            img1 = itemView.findViewById(R.id.img1);
//            img2 = itemView.findViewById(R.id.img2);
//            img3 = itemView.findViewById(R.id.img3);
//            img4 = itemView.findViewById(R.id.img4);
//            img5 = itemView.findViewById(R.id.img5);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnInfo = itemView.findViewById(R.id.btnInfo);

        }
    }
}
