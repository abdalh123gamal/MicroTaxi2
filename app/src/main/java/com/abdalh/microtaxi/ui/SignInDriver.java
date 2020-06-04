package com.abdalh.microtaxi.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.abdalh.microtaxi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import dmax.dialog.SpotsDialog;

public class SignInDriver extends AppCompatActivity {
    private FirebaseAuth auth;
    ProgressDialog pd;
    ConstraintLayout sign_in_driver;
    ProgressBar progressBar;

    private EditText ed_email,ed_password;
    private Button btn_login, btn_register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_driver);
        sign_in_driver=findViewById(R.id.sign_in_driver);

        btn_login=findViewById(R.id.sign_in_driver_btn_login);
        btn_register=findViewById(R.id.sign_in_driver_btn_register);
        ed_email=findViewById(R.id.sign_in_driver_ed_email);
        ed_password=findViewById(R.id.sign_in_driver_ed_password);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDriver();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplication(), RegisterDriver.class));
            }
        });
    }
    public  void loginDriver() {

        final AlertDialog dialog = new SpotsDialog(SignInDriver.this,"جاري تسجيل الدخول ..",R.style.CustomDialog);
        dialog.show();

        auth= FirebaseAuth.getInstance();

        String email =ed_email.getText().toString();
        String password =ed_password.getText().toString();

        if (!email.isEmpty()&&!password.isEmpty()){

            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        startActivity(new Intent(getApplication(),DriverHome.class));
                        finish();

                    }

                    else {
                        dialog.dismiss();
                        Snackbar.make(sign_in_driver,"بريد إلكتروني غير صحيح أو كلمة سر خاطئة",Snackbar.LENGTH_LONG).show();



                    }

                }

            });

        }
        else if (email.isEmpty()&&password.isEmpty()){

            TextUtils.isEmpty(email);
            ed_email.setError("يجب إدخال بريد إلكتروني");
            TextUtils.isEmpty(password);
            ed_password.setError("أدخل كلمة المرور ");
            return;
        }
        else if (password.isEmpty()){

            TextUtils.isEmpty(password);
            ed_password.setError("أدخل كلمة المرور ");
            return;
        }
    }

}
