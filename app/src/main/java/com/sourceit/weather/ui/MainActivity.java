package com.sourceit.weather.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sourceit.weather.App;
import com.sourceit.weather.R;
import com.sourceit.weather.ui.WeatherSystem.Weather;
import com.sourceit.weather.ui.WeatherSystem.WeatherSystem;
import com.sourceit.weather.utils.L;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    Resources res = App.getApp().getResources();
    public final String MY_CUSTOM_INTENT = res.getString(R.string.my_custom_intent);

    private WeatherSystem localWeatherSystem;
    public static String currentTemp;

    private RecyclerView weatherList;
    private LinearLayoutManager linearLayoutManagerl;

    TextView weatherDesc;
    TextView temperature;
    TextView temperatureFeel;
    TextView pressure;
    TextView windSpeed;
    TextView sunriseSunset;
    TextView moonriseMoonset;
    TextView humidity;
    TextView cloudcover;
    TextView precipMM;
    TextView windgustkmph;
    ImageView icon;
    TextView rain;
    TextView snow;
    TextView fog;

    public static final int FIRST = 0;
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;

    private Button settings;
    private Intent intent;
    private Gson gson;
    private Calendar calendar;
    private boolean paused;

    private void setRetrofit() {
        L.d("retrofit with city: " + sp.getString(res.getString(R.string.city), ""));
        if (sp.getString(res.getString(R.string.city), "").equals("")) {
            retrofit(res.getString(R.string.kharkov));
        } else {
            retrofit(sp.getString(res.getString(R.string.city), ""));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                finish();
            }
        });

        L.d("file existance: " + fileExistance(res.getString(R.string.json_weather)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.d("onResume");
        L.d("paused: " + paused);
        L.d("current year: " + calendar.get(Calendar.YEAR));
        L.d("saved year: " + sp.getInt(res.getString(R.string.savedyear), 0));
        L.d("current day: " + calendar.get(Calendar.DAY_OF_MONTH));
        L.d("saved day: " + sp.getInt(res.getString(R.string.savedday), 0));
        L.d("current month: " + calendar.get(Calendar.MONTH));
        L.d("saved month: " + sp.getInt(res.getString(R.string.savedmonth), 0));
        if (!fileExistance(res.getString(R.string.json_weather)) || sp.getInt(res.getString(R.string.savedmonth), 0) != calendar.get(Calendar.MONTH)
                || sp.getInt(res.getString(R.string.savedday), 0) != calendar.get(Calendar.DAY_OF_MONTH) || sp.getInt(res.getString(R.string.savedyear), 0) != calendar.get(Calendar.YEAR)) {
            setRetrofit();
        } else {
            if (!paused) {
                if (sp.getBoolean(res.getString(R.string.update), false)) {
                    L.d("update state true");
                    editor.putBoolean(res.getString(R.string.update), false);
                    editor.apply();
                    setRetrofit();
                } else if (sp.getBoolean(res.getString(R.string.change), false)) {
                    editor.putBoolean(res.getString(R.string.change), false);
                    editor.apply();
                    setRetrofit();
                } else {
                    readFile();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    private void init() {

        sp = getPreferences(Context.MODE_PRIVATE);
        editor = sp.edit();

        weatherList = (RecyclerView) findViewById(R.id.weather_recyclerlist);
        linearLayoutManagerl = new LinearLayoutManager(this);
        weatherList.setLayoutManager(linearLayoutManagerl);

        weatherDesc = (TextView) findViewById(R.id.weatherDesc);
        temperature = (TextView) findViewById(R.id.temperature_value);
        temperatureFeel = (TextView) findViewById(R.id.temperature_feel_value);
        pressure = (TextView) findViewById(R.id.pressure_value);
        windSpeed = (TextView) findViewById(R.id.windspeed_value);
        sunriseSunset = (TextView) findViewById(R.id.sunrise_and_sunset_value);
        moonriseMoonset = (TextView) findViewById(R.id.moonrise_and_moonset_value);
        humidity = (TextView) findViewById(R.id.humidity_value);
        cloudcover = (TextView) findViewById(R.id.cloudcover_value);
        precipMM = (TextView) findViewById(R.id.precipmm_value);
        windgustkmph = (TextView) findViewById(R.id.windgustkmph_value);
        icon = (ImageView) findViewById(R.id.icon);
        rain = (TextView) findViewById(R.id.chanceofrain_value);
        snow = (TextView) findViewById(R.id.chanceofsnow_value);
        fog = (TextView) findViewById(R.id.chanceoffog_value);

        settings = (Button) findViewById(R.id.button_settings);
        intent = new Intent(this, SettingsActivity.class);
        gson = new Gson();
        calendar = Calendar.getInstance();
    }

    private void sendMessage() {
        Intent intentReceiver = new Intent();
        intentReceiver.setAction(MY_CUSTOM_INTENT);
        sendBroadcast(intentReceiver);
        L.d("send broadcast");
    }

    private void retrofit(String city) {
        L.d("check city in retrofit method: " + city);
        Retrofit.getData(city, new Callback<WeatherSystem>() {
            @Override
            public void success(WeatherSystem weatherSystem, Response response) {
                String temp = new String();
                try {
                    temp = weatherSystem.data.error.get(FIRST).msg;
                } catch (NullPointerException e) {
                }
                L.d("error string: " + temp);
                if (temp.equals("")) {
                    localWeatherSystem = weatherSystem;
                    if (!paused) {
                        L.d("setAdapter");
                        setAdapter();
                    } else {
                        L.d("notifyDataSetChanged");
                        weatherList.getAdapter().notifyDataSetChanged();
                    }
                    saveFile();

                    editor.putInt(res.getString(R.string.savedday), calendar.get(Calendar.DAY_OF_MONTH));
                    editor.putInt(res.getString(R.string.savedmonth), calendar.get(Calendar.MONTH));
                    editor.putInt(res.getString(R.string.savedyear), calendar.get(Calendar.YEAR));
                    editor.apply();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.enter_correct_city_in_english, Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), R.string.error_downloading_from_server, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setAdapter() {

        setData(FIRST);

        weatherList.setAdapter(new RecyclerAdapter(localWeatherSystem.data.weather, new OnItemClickWatcher<Weather>() {
            @Override
            public void onItemClick(View v, int position, Weather item) {
                setData(position);
            }
        }));
    }

    public void setData(int value) {

        weatherDesc.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).lang_ru.get(FIRST).value);

        if (sp.getBoolean(res.getString(R.string.degreesset), false)) {
            temperature.setText(String.format("%s%s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).tempF, res.getString(R.string.f)));
            temperatureFeel.setText(String.format("%s%s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).FeelsLikeF, res.getString(R.string.f)));
            currentTemp = temperature.getText().toString();
            sendMessage();
        } else {
            temperature.setText(String.format("%s%s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).tempC, res.getString(R.string.c)));
            temperatureFeel.setText(String.format("%s%s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).FeelsLikeC, res.getString(R.string.c)));
            currentTemp = temperature.getText().toString();
            sendMessage();
        }
        pressure.setText(String.format("%s %s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).pressure, res.getString(R.string.pressure_value)));
        windSpeed.setText(String.format("%s %s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).windspeedKmph, res.getString(R.string.kmph)));
        sunriseSunset.setText(localWeatherSystem.data.weather.get(value).astronomy.get(FIRST).sunrise
                + " - " + localWeatherSystem.data.weather.get(value).astronomy.get(FIRST).sunset);
        moonriseMoonset.setText(localWeatherSystem.data.weather.get(value).astronomy.get(FIRST).moonrise
                + " - " + localWeatherSystem.data.weather.get(value).astronomy.get(FIRST).moonset);
        humidity.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).humidity + "%");
        cloudcover.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).cloudcover + "%");
        precipMM.setText(String.format("%s %s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).precipMM, res.getString(R.string.mm)));
        windgustkmph.setText(String.format("%s %s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).WindGustKmph, res.getString(R.string.kmph)));
        Picasso.with(this).load(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).weatherIconUrl.get(FIRST).value)
                .placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(icon);
        rain.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).chanceofrain + "%");
        fog.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).chanceoffog + "%");
        snow.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).chanceofsnow + "%");
    }

    private void saveFile() {
        new MyAsyncTask().execute(res.getString(R.string.write));
    }

    private boolean fileExistance(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    private void readFile() {
        new MyAsyncTask().execute(res.getString(R.string.read));
    }

    private class MyAsyncTask extends AsyncTask<String, Void, Void> {
        boolean setAdapter;

        @Override
        protected Void doInBackground(String... params) {
            if (params[0].equals(res.getString(R.string.write))) {
                L.d("WRITE");
                try {
                    String jsonWeather = gson.toJson(localWeatherSystem);
                    FileOutputStream fos = openFileOutput(res.getString(R.string.json_weather), Context.MODE_PRIVATE);
                    fos.write(jsonWeather.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (params[0].equals(res.getString(R.string.read))) {
                L.d("READ");
                setAdapter = true;
                localWeatherSystem = new WeatherSystem();
                try {
                    FileInputStream fis = openFileInput(res.getString(R.string.json_weather));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                    StringBuilder text = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        text.append(line);
                        fis.close();
                        localWeatherSystem = gson.fromJson(text.toString(), localWeatherSystem.getClass());
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (setAdapter) {
                setAdapter();
            }
        }
    }
}