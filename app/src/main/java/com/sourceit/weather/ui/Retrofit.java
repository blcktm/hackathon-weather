package com.sourceit.weather.ui;

import android.content.res.Resources;

import com.sourceit.weather.App;
import com.sourceit.weather.R;
import com.sourceit.weather.ui.WeatherSystem.WeatherSystem;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Aleksey on 22.02.2016.
 */
public class Retrofit {

    static Resources res = App.getApp().getResources();

    public static final String Q = "q";
    public static final String FORMAT = "format";
    public static final String NUM_OF_DAYS = "num_of_days";
    public static final String TP = "tp";
    public static final String LANG = "lang";
    public static final String KEY = "key";
    public static final String HTTP_API_WORLDWEATHERONLINE_COM_FREE_V2 = "http://api.worldweatheronline.com/free/v2";

    private static String ENDPOINT = HTTP_API_WORLDWEATHERONLINE_COM_FREE_V2;
    private static ApiInterface apiInterface;

    static {
        init();
    }

    interface ApiInterface {

        @GET("/weather.ashx")
        void getData(
                @Query(Q) String city,
                @Query(FORMAT) String format,
                @Query(NUM_OF_DAYS) int num_of_days,
                @Query(TP) int tp,
                @Query(LANG) String lang,
                @Query(KEY) String key,
                Callback<WeatherSystem> dataCallback);
    }

    public static void init() {
        RestAdapter ra = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        apiInterface = ra.create(ApiInterface.class);
    }

    public static void getData(String city, Callback<WeatherSystem> dataCallback) {
        apiInterface.getData(city, res.getString(R.string.retrofit_json), 5, 24, res.getString(R.string.retrofit_ru), res.getString(R.string.retrofit_key), dataCallback);
    }
}
