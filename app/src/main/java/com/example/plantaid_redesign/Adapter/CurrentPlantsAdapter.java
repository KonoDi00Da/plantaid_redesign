package com.example.plantaid_redesign.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantaid_redesign.Model.AllPlantsList;
import com.example.plantaid_redesign.Model.User_Plants;
import com.example.plantaid_redesign.MyGarden.UserMyGardenPlantsActivity;
import com.example.plantaid_redesign.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CurrentPlantsAdapter extends RecyclerView.Adapter<CurrentPlantsAdapter.MyViewHolder> {
    Context context;
    ArrayList<User_Plants> list;
    NavController navController;

    public CurrentPlantsAdapter(ArrayList<User_Plants> list, Context context, NavController navController) {
        this.list = list;
        this.context = context;
        this.navController = navController;
    }

    @NonNull
    @Override
    public CurrentPlantsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_user_garden_plants, parent, false);
        return new CurrentPlantsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentPlantsAdapter.MyViewHolder holder, int position) {
        User_Plants model = list.get(position);

        Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_launcher_foreground).into(holder.imageView);
        holder.commonName.setText(model.getC_plantName());
        holder.sciName.setText(model.getS_plantName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserMyGardenPlantsActivity.class);
                intent.putExtra("plant_image", model.getImage());
                intent.putExtra("commonName", model.getC_plantName());
                intent.putExtra("userKey", model.getUser_key());
                intent.putExtra("plantKey", model.getKey());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
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

        ImageView imageView;
        TextView commonName, sciName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imgPlant);
            commonName = itemView.findViewById(R.id.txtComPlantName);
            sciName = itemView.findViewById(R.id.txtSciPlantName);

        }
    }
}
