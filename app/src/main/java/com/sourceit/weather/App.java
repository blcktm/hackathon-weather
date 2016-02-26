package com.sourceit.weather;

import android.app.Application;

/**
 * Created by User on 23.02.2016.
 */
public class App extends Application {
    private static App instance;

    public static App getApp() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
