package com.abdalh.microtaxi.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.abdalh.microtaxi.R;

public class ActivitySelectType extends AppCompatActivity {
    ImageView iv_driver,iv_passenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_type);
        iv_driver=findViewById(R.id.activity_select_type_iv_driver);
        iv_passenger=findViewById(R.id.activity_select_type_iv_passenger);
        iv_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplication(), SignInDriver.class));

            }
        });
        iv_passenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplication(), SignInRider.class));

            }
        });
    }
}
