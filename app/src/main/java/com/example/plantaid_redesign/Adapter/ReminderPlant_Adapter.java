package com.example.plantaid_redesign.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantaid_redesign.Model.PlantReminderModel;
import com.example.plantaid_redesign.MyGarden.PlantCare_Edit_Reminder;
import com.example.plantaid_redesign.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ReminderPlant_Adapter extends RecyclerView.Adapter<ReminderPlant_Adapter.MyViewHolder> {
    Context context;
    ArrayList<PlantReminderModel> list;

    public ReminderPlant_Adapter(ArrayList<PlantReminderModel> list, Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ReminderPlant_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_plant_reminder_1,parent,false);
        return new ReminderPlant_Adapter.MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ReminderPlant_Adapter.MyViewHolder holder, int position) {
        //assigning the values to the recyclerview
        PlantReminderModel model = list.get(position);


        holder.txtTask.setText(model.getReminderType());
        holder.txtTime.setText(model.getTime().replace("_"," "));
        holder.txtDate.setText(model.getDate().replace("_"," "));

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlantCare_Edit_Reminder.class);
                intent.putExtra("plant_name",model.getPlantName());
                intent.putExtra("time",model.getTime());
                intent.putExtra("reminder",model.getReminderType());
                intent.putExtra("date",model.getDate());
                intent.putExtra("user_Key",model.getUserKey());
                intent.putExtra("reminder_key",model.getReminderKey());
                context.startActivity(intent);
            }
        });


    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //grabbing views from the recyclerview layout
        //similar to onCreate method

        TextView txtTask, txtTime, txtDate;
        ImageView btnEdit;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTask = itemView.findViewById(R.id.txtTask);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);

        }
    }

}
