package com.example.plantaid_redesign.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantaid_redesign.Model.AllPlantsList;
import com.example.plantaid_redesign.MyGarden.MyGardenPlantsDetails;
import com.example.plantaid_redesign.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AllPlantListAdapter extends RecyclerView.Adapter<AllPlantListAdapter.MyViewHolder> {

    Context context;
    ArrayList<AllPlantsList> list;
    NavController  navController;

    public AllPlantListAdapter(ArrayList<AllPlantsList> list, Context context, NavController navController){
        this.list = list;
        this.context = context;
        this.navController = navController;
    }

    @NonNull
    @Override
    public AllPlantListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //This is where we'll be inflating the recyclerview (looks of the row)
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_all_plants,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllPlantListAdapter.MyViewHolder holder, int position) {
        //assigning the values to the recyclerview
        AllPlantsList model = list.get(position);

        Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_launcher_foreground).into(holder.imgPlant);
        holder.txtComPlantName.setText(model.getCommonName());
        holder.txtSciPlantName.setText(model.getSciName());
        //holder.setListener((view, po))

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a Bundle object and add the model values to it
                Bundle bundle = new Bundle();
                bundle.putString("plant_image", model.getImage());
                bundle.putString("com_plant", model.getCommonName());
                bundle.putString("sci_plant", model.getSciName());
                bundle.putString("txtWater", model.getWater());
                bundle.putString("txtHarvest", model.getHarvest());
                bundle.putString("txtCare", model.getCare());
                bundle.putString("plant_desc",model.getDescription());
                bundle.putString("txtPestsDisease",model.getPestsDiseases());
                bundle.putString("ytLink",model.getYtLink());
                bundle.putString("key",model.getKey());

                // Create a new instance of the target fragment and set the Bundle as its arguments
//                MyGardenPlantsDetails fragment = new MyGardenPlantsDetails();
//                fragment.setArguments(bundle);

                // Replace the current fragment with the target fragment
                navController.navigate(R.id.action_myGardenAllPlantsFragment_to_myGardenPlantsDetails,bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        //grabbing views from the recyclerview layout
        //similar to onCreate method

        ImageView imgPlant;
        TextView txtComPlantName, txtSciPlantName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPlant = itemView.findViewById(R.id.imgPlant);
            txtComPlantName = itemView.findViewById(R.id.txtComPlantName);
            txtSciPlantName = itemView.findViewById(R.id.txtSciPlantName);

        }
    }


}
