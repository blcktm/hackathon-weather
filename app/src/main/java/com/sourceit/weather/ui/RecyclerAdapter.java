package com.sourceit.weather.ui;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sourceit.weather.App;
import com.sourceit.weather.R;
import com.sourceit.weather.ui.WeatherSystem.Weather;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by User on 24.02.2016.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener {

    Resources res = App.getApp().getResources();

    private ArrayList<Weather> objects;
    private OnItemClickWatcher<Weather> watcher;

    public RecyclerAdapter(ArrayList<Weather> objects, OnItemClickWatcher<Weather> watcher) {
        this.objects = objects;
        this.watcher = watcher;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.model, parent, false);
        return new ViewHolder(v, watcher, objects);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.pressure.setText(String.format("%s %s", objects.get(position).hourly.get(MainActivity.FIRST).pressure, res.getString(R.string.pressure_value)));
        if (!MainActivity.sp.getBoolean(res.getString(R.string.degreesset), false)) {
            holder.temperatureFeel.setText(String.format("%s%s", objects.get(position).hourly.get(MainActivity.FIRST).FeelsLikeC, res.getString(R.string.c)));
            holder.temperature.setText(String.format("%s%s", objects.get(position).hourly.get(MainActivity.FIRST).tempC, res.getString(R.string.c)));
        } else {
            holder.temperatureFeel.setText(String.format("%s%s", objects.get(position).hourly.get(MainActivity.FIRST).FeelsLikeF, res.getString(R.string.f)));
            holder.temperature.setText(String.format("%s%s", objects.get(position).hourly.get(MainActivity.FIRST).tempF, res.getString(R.string.f)));
        }
        holder.weatherDesc.setText(objects.get(position).hourly.get(MainActivity.FIRST).lang_ru.get(MainActivity.FIRST).value);
        holder.date.setText(objects.get(position).date);

        Picasso.with(holder.icon.getContext()).load(objects.get(position).hourly.get(MainActivity.FIRST).weatherIconUrl.get(MainActivity.FIRST).value)
                .placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(holder.icon);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public void onClick(View v) {
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.weatherDesc_inlist)
        TextView weatherDesc;
        @BindView(R.id.temperature_value_inlist)
        TextView temperature;
        @BindView(R.id.temperature_feel_value_inlist)
        TextView temperatureFeel;
        @BindView(R.id.pressure_value_inlist)
        TextView pressure;
        @BindView(R.id.icon_for_list)
        ImageView icon;
        @BindView(R.id.date_inlist)
        TextView date;

        public ViewHolder(View item, final OnItemClickWatcher<Weather> watcher, final ArrayList<Weather> objects) {
            super(item);
            ButterKnife.bind(this, item);

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    watcher.onItemClick(v, getAdapterPosition(), objects.get(getAdapterPosition()));
                }
            });
        }
    }
}
