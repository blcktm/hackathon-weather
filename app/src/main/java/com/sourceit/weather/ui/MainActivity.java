package com.sourceit.weather.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public static final String C = "\u2103";
    public static final String F = "\u2109";
    public static final String DEGREESSET = "degreesset";
    public static final String JSON_WEATHER = "json_weather";
    public static final String SAVEDDAY = "savedday";
    public static final String SAVEDMONTH = "savedmonth";
    public static final String SAVEDYEAR = "savedyear";
    public static final String WRITE = "write";
    public static final String READ = "read";
    public static final String CHANGE = "change";
    public static final String KHARKOV = "Kharkov";
    public static final String CITY = "city";
    public static final String MY_CUSTOM_INTENT = "my.custom.INTENT";

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
    public static final String PRESSURE_VALUE = "гПа";
    public static final String KMPH = "км/ч";
    public static final String MM = "мм";
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;

    private Button settings;
    private Intent intent;
    private Gson gson;
    private Calendar calendar;
    private boolean paused;

    private void setRetrofit() {
        L.d("retrofit with city: " + sp.getString(CITY, ""));
        if (sp.getString(CITY, "").equals("")) {
            retrofit(KHARKOV);
        } else {
            retrofit(sp.getString(CITY, ""));
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

        L.d("file existance: " + fileExistance(JSON_WEATHER));
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.d("onResume");
        L.d("paused: " + paused);
        L.d("current year: " + calendar.get(Calendar.YEAR));
        L.d("saved year: " + sp.getInt(SAVEDYEAR, 0));
        L.d("current day: " + calendar.get(Calendar.DAY_OF_MONTH));
        L.d("saved day: " + sp.getInt(SAVEDDAY, 0));
        L.d("current month: " + calendar.get(Calendar.MONTH));
        L.d("saved month: " + sp.getInt(SAVEDMONTH, 0));
        if (!fileExistance(JSON_WEATHER) || sp.getInt(SAVEDMONTH, 0) != calendar.get(Calendar.MONTH)
                || sp.getInt(SAVEDDAY, 0) != calendar.get(Calendar.DAY_OF_MONTH) || sp.getInt(SAVEDYEAR, 0) != calendar.get(Calendar.YEAR)) {
            setRetrofit();
        } else {
            if (!paused) {
                if (sp.getBoolean(SettingsActivity.UPDATE, false)) {
                    L.d("update state true");
                    editor.putBoolean(SettingsActivity.UPDATE, false);
                    editor.apply();
                    setRetrofit();
                } else if (sp.getBoolean(CHANGE, false)) {
                    editor.putBoolean(CHANGE, false);
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

                    editor.putInt(SAVEDDAY, calendar.get(Calendar.DAY_OF_MONTH));
                    editor.putInt(SAVEDMONTH, calendar.get(Calendar.MONTH));
                    editor.putInt(SAVEDYEAR, calendar.get(Calendar.YEAR));
                    editor.apply();
                } else {
                    Toast.makeText(getApplicationContext(), "enter correct city in english!", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "error downloading from server", Toast.LENGTH_LONG).show();
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

        if (sp.getBoolean(DEGREESSET, false)) {
            temperature.setText(String.format("%s%s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).tempF, F));
            temperatureFeel.setText(String.format("%s%s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).FeelsLikeF, F));
            currentTemp = temperature.getText().toString();
            sendMessage();
        } else {
            temperature.setText(String.format("%s%s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).tempC, C));
            temperatureFeel.setText(String.format("%s%s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).FeelsLikeC, C));
            currentTemp = temperature.getText().toString();
            sendMessage();
        }
        pressure.setText(String.format("%s %s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).pressure, PRESSURE_VALUE));
        windSpeed.setText(String.format("%s %s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).windspeedKmph, KMPH));
        sunriseSunset.setText(localWeatherSystem.data.weather.get(value).astronomy.get(FIRST).sunrise
                + " - " + localWeatherSystem.data.weather.get(value).astronomy.get(FIRST).sunset);
        moonriseMoonset.setText(localWeatherSystem.data.weather.get(value).astronomy.get(FIRST).moonrise
                + " - " + localWeatherSystem.data.weather.get(value).astronomy.get(FIRST).moonset);
        humidity.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).humidity + "%");
        cloudcover.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).cloudcover + "%");
        precipMM.setText(String.format("%s %s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).precipMM, MM));
        windgustkmph.setText(String.format("%s %s", localWeatherSystem.data.weather.get(value).hourly.get(FIRST).WindGustKmph, KMPH));
        Picasso.with(this).load(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).weatherIconUrl.get(FIRST).value)
                .placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(icon);
        rain.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).chanceofrain + "%");
        fog.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).chanceoffog + "%");
        snow.setText(localWeatherSystem.data.weather.get(value).hourly.get(FIRST).chanceofsnow + "%");
    }

    private void saveFile() {
        new MyAsyncTask().execute(WRITE);
    }

    private boolean fileExistance(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    private void readFile() {
        new MyAsyncTask().execute(READ);
    }

    private class MyAsyncTask extends AsyncTask<String, Void, Void> {
        boolean setAdapter;

        @Override
        protected Void doInBackground(String... params) {
            if (params[0].equals(WRITE)) {
                L.d("WRITE");
                try {
                    String jsonWeather = gson.toJson(localWeatherSystem);
                    FileOutputStream fos = openFileOutput(JSON_WEATHER, Context.MODE_PRIVATE);
                    fos.write(jsonWeather.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (params[0].equals(READ)) {
                L.d("READ");
                setAdapter = true;
                localWeatherSystem = new WeatherSystem();
                try {
                    FileInputStream fis = openFileInput(JSON_WEATHER);
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