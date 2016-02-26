package com.sourceit.weather.ui;

import android.view.View;

/**
 * Created by User on 24.02.2016.
 */
public abstract class OnItemClickWatcher<T> {
    public abstract void onItemClick(View v, int position, T item);
}
