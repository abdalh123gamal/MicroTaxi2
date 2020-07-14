package com.abdalh.microtaxi.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.abdalh.microtaxi.R;
import com.abdalh.microtaxi.model.Rider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivitySelectType extends AppCompatActivity {
    ImageView iv_driver,iv_passenger;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_type);
        iv_driver=findViewById(R.id.activity_select_type_iv_driver);
        iv_passenger=findViewById(R.id.activity_select_type_iv_passenger);

         startService(new Intent(ActivitySelectType.this,OnAppKilled.class));
    iv_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth=FirebaseAuth.getInstance();
                FirebaseUser firebaseUser=auth.getCurrentUser();
                if(firebaseUser==null){
                    startActivity(new Intent(getApplication(), SignInDriver.class));

                }

                else {

                    Intent intent=new Intent(getApplication(),DriverHome.class);
                    startActivity(intent);
                    finish();
                }


            }
        });

        iv_passenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth=FirebaseAuth.getInstance();
                FirebaseUser firebaseUser=auth.getCurrentUser();
                if(firebaseUser==null){
                    startActivity(new Intent(getApplication(), SignInRider.class));

                }

                else {

                    Intent intent=new Intent(getApplication(),RiderHome.class);
                    startActivity(intent);
                    finish();
                }


            }
        });
    }
}
