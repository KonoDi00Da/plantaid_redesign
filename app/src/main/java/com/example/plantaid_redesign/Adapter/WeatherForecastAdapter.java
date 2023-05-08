package com.example.plantaid_redesign.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantaid_redesign.Common.Common;
import com.example.plantaid_redesign.Model.WeatherForecastResult;
import com.example.plantaid_redesign.R;
import com.squareup.picasso.Picasso;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.MyViewHolder> {

    Context context;
    WeatherForecastResult weatherForecastResult;

    public WeatherForecastAdapter(Context context, WeatherForecastResult weatherForecastResult) {
        this.context = context;
        this.weatherForecastResult = weatherForecastResult;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_weather_forecast,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //Load icon
        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                .append(weatherForecastResult.list.get(position).weather.get(0).getIcon())
                .append(".png").toString()).into(holder.imgWeatherIcon);
        holder.txt_day_of_week.setText(new StringBuilder(Common.convertUnixToDay(weatherForecastResult.list.get(position).dt)));
        holder.txtDescription.setText(new StringBuilder(weatherForecastResult.list.get(position).weather.get(0).getDescription()));
        holder.txtTemperature.setText(new StringBuilder(String.valueOf(weatherForecastResult.list.get(position).main.getTemp())).append("°C"));
    }

    @Override
    public int getItemCount() {
        return weatherForecastResult.list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView txt_day_of_week, txtTemperature, txtDescription;
        ImageView imgWeatherIcon;
        public MyViewHolder (View itemView){
            super(itemView);

            txt_day_of_week = (TextView) itemView.findViewById(R.id.txt_day_of_week);
            txtTemperature = (TextView) itemView.findViewById(R.id.txtTemperature);
            txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
            imgWeatherIcon = (ImageView) itemView.findViewById(R.id.imgWeatherIcon);

        }
    }

}
