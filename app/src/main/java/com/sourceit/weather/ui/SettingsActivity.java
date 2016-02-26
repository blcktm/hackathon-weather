package com.sourceit.weather.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sourceit.weather.R;
import com.sourceit.weather.utils.L;

public class SettingsActivity extends AppCompatActivity {

    public static final String UPDATE = "update";

    Button degreesSetC;
    Button degreesSetF;
    Button update;
    Button back;
    EditText city;
    Button applyCity;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        init();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                finish();
            }
        });

        degreesSetC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.sp.getBoolean(MainActivity.DEGREESSET, false)) {
                    MainActivity.editor.putBoolean(MainActivity.DEGREESSET, false);
                    MainActivity.editor.apply();
                    L.d("degrees state in settings: " + MainActivity.sp.getBoolean(MainActivity.DEGREESSET, false));
                }
            }
        });

        degreesSetF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.sp.getBoolean(MainActivity.DEGREESSET, false)) {
                    MainActivity.editor.putBoolean(MainActivity.DEGREESSET, true);
                    MainActivity.editor.apply();
                    L.d("degrees state in settings: " + MainActivity.sp.getBoolean(MainActivity.DEGREESSET, false));
                }
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.sp.getBoolean(UPDATE, false)) {
                    MainActivity.editor.putBoolean(UPDATE, true);
                    MainActivity.editor.apply();
                }
                Toast.makeText(getApplicationContext(), "update after BACK", Toast.LENGTH_LONG).show();
            }
        });

        applyCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (!MainActivity.sp.getBoolean(MainActivity.CHANGE, false)) {
                        MainActivity.editor.putBoolean(MainActivity.CHANGE, true);
                    }
                    String cityText = city.getText().toString().substring(0, 1).toUpperCase() + city.getText().toString().substring(1, city.getText().length()).toLowerCase();
                    MainActivity.editor.putString(MainActivity.CITY, cityText);
                    MainActivity.editor.apply();
                    L.d("city: " + cityText);
                    Toast.makeText(getApplicationContext(), "change after BACK", Toast.LENGTH_LONG).show();
                    city.setText("");
                } catch (IndexOutOfBoundsException e) {
                    Toast.makeText(getApplicationContext(), "enter text!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void init() {
        update = (Button) findViewById(R.id.button_update);
        degreesSetC = (Button) findViewById(R.id.button_cset);
        degreesSetF = (Button) findViewById(R.id.button_fset);
        back = (Button) findViewById(R.id.button_back);
        intent = new Intent(this, MainActivity.class);
        city = (EditText) findViewById(R.id.entercity);
        applyCity = (Button) findViewById(R.id.button_apply);
    }
}
