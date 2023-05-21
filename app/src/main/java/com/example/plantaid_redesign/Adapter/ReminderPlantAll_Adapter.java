package com.example.plantaid_redesign.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantaid_redesign.Model.PlantReminderModel;
import com.example.plantaid_redesign.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ReminderPlantAll_Adapter extends RecyclerView.Adapter<ReminderPlantAll_Adapter.MyViewHolder> {
    Context context;
    ArrayList<PlantReminderModel> list;

    public ReminderPlantAll_Adapter(ArrayList<PlantReminderModel> list, Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ReminderPlantAll_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_plant_reminder_today,parent,false);
        return new ReminderPlantAll_Adapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderPlantAll_Adapter.MyViewHolder holder, int position) {
        //assigning the values to the recyclerview
        PlantReminderModel model = list.get(position);

        holder.txtPlantTask.setText(model.getReminderType());
        holder.txtPlantName.setText(model.getPlantName());
        holder.txtTime.setText(model.getTime().replace("_"," "));
        holder.txtDate.setText(model.getDate().replace("_"," "));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //grabbing views from the recyclerview layout
        //similar to onCreate method

        TextView txtPlantTask, txtTime, txtDate, txtPlantName;
        FloatingActionButton btnEdit;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPlantTask = itemView.findViewById(R.id.txtTask);
            txtPlantName = itemView.findViewById(R.id.txtPlantName);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDate = itemView.findViewById(R.id.txtDate);

        }
    }
}
