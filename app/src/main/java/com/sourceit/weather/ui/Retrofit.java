package com.sourceit.weather.ui;

import com.sourceit.weather.ui.WeatherSystem.WeatherSystem;
import com.sourceit.weather.utils.L;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Aleksey on 22.02.2016.
 */
public class Retrofit {
    private static String ENDPOINT = "http://api.worldweatheronline.com/free/v2";
    private static ApiInterface apiInterface;

    static {
        init();
    }

    interface ApiInterface {
        @GET("/{city}")
        void getData(@Path(value = "city", encode = false) String name, Callback<WeatherSystem> dataCallback);
    }

    public static void init() {
        RestAdapter ra = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        apiInterface = ra.create(ApiInterface.class);
    }

    public static void getData(String city, Callback<WeatherSystem> dataCallback) {
        apiInterface.getData("weather.ashx?q=" + city + "&format=json&num_of_days=5&tp=24&lang=ru&key=306851eef2772c27ce397089ae388", dataCallback);
        L.d("format url: " + "weather.ashx?q=" + city + "&format=json&num_of_days=5&tp=24&lang=ru&key=306851eef2772c27ce397089ae388");
    }
}
