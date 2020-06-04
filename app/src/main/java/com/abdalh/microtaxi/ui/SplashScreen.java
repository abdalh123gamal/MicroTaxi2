package com.abdalh.microtaxi.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.abdalh.microtaxi.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {
    FirebaseAuth auth;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Toast.makeText(getApplicationContext(),R.string.main_toast_application_in_progress,Toast.LENGTH_LONG).show();

        progressBar=findViewById(R.id.splash_progress_bar);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                auth=FirebaseAuth.getInstance();
//                FirebaseUser firebaseUser=auth.getCurrentUser();
//                if(firebaseUser==null){
                    Intent intent=new Intent(getApplication(), ActivitySelectType.class);
                    startActivity(intent);
                    finish();
//                }
//
//                else {
//
//                    Intent intent=new Intent(getApplication(),DriverHome.class);
//                    startActivity(intent);
//                    finish();
//                }





            }
        },2000);

    }



}
