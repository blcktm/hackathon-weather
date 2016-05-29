package com.sourceit.weather.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.sourceit.weather.App;
import com.sourceit.weather.R;
import com.sourceit.weather.utils.L;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    Resources res = App.getApp().getResources();
    @BindView(R.id.entercity)
    EditText city;

    @OnClick(R.id.button_cset)
    public void buttonCset() {
        if (MainActivity.sp.getBoolean(res.getString(R.string.degreesset), false)) {
            MainActivity.editor.putBoolean(res.getString(R.string.degreesset), false);
            MainActivity.editor.apply();
            L.d("degrees state in settings: " + MainActivity.sp.getBoolean(res.getString(R.string.degreesset), false));
        }
    }

    @OnClick(R.id.button_fset)
    public void buttonFset() {
        if (!MainActivity.sp.getBoolean(res.getString(R.string.degreesset), false)) {
            MainActivity.editor.putBoolean(res.getString(R.string.degreesset), true);
            MainActivity.editor.apply();
            L.d("degrees state in settings: " + MainActivity.sp.getBoolean(res.getString(R.string.degreesset), false));
        }
    }

    @OnClick(R.id.button_update)
    public void buttonUpdate() {
        if (!MainActivity.sp.getBoolean(res.getString(R.string.update), false)) {
            MainActivity.editor.putBoolean(res.getString(R.string.update), true);
            MainActivity.editor.apply();
        }
        Toast.makeText(getApplicationContext(), R.string.update_after_back, Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.button_back)
    public void buttonBack() {
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.button_apply)
    public void buttonAplly() {
        try {
            if (!MainActivity.sp.getBoolean(res.getString(R.string.change), false)) {
                MainActivity.editor.putBoolean(res.getString(R.string.change), true);
            }
            String cityText = city.getText().toString().substring(0, 1).toUpperCase() + city.getText().toString().substring(1, city.getText().length()).toLowerCase();
            MainActivity.editor.putString(res.getString(R.string.city), cityText);
            MainActivity.editor.apply();
            L.d("city: " + cityText);
            Toast.makeText(getApplicationContext(), R.string.change_after_back, Toast.LENGTH_LONG).show();
            city.setText("");
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(getApplicationContext(), R.string.enter_text, Toast.LENGTH_LONG).show();
        }
    }

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        ButterKnife.bind(this);

        intent = new Intent(this, MainActivity.class);
    }
}
