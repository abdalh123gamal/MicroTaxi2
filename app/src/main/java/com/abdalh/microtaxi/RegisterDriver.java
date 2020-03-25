package com.abdalh.microtaxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterDriver extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth auth;
    DatabaseReference databaseReference;

    EditText ed_email,ed_name,ed_phone,ed_password,ed_car_type;
    ProgressBar progressBar;
    Button btn_register;
    ConstraintLayout register_driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);
        register_driver=findViewById(R.id.register_driver);


        ed_name=findViewById(R.id.register_driver_ed_name);
        ed_email=findViewById(R.id.register_driver_ed_email);
        ed_phone=findViewById(R.id.register_driver_ed_phone);
        ed_password=findViewById(R.id.register_driver_ed_password);
        ed_car_type=findViewById(R.id.register_driver_ed_car_type);
        progressBar=findViewById(R.id.register_driver_progress_bar);
        btn_register=findViewById(R.id.register_driver_btn_register);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDriver();
            }
        });

    }
    public void registerDriver(){

        auth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Drivers");

        final String name =ed_name.getText().toString();
        final String email =ed_email.getText().toString();
        final String phone =ed_phone.getText().toString();
        final String password =ed_password.getText().toString();
        final String car_type =ed_car_type.getText().toString();
        if(!name.isEmpty()&&!email.isEmpty()&&!phone.isEmpty()&&!password.isEmpty()&&!car_type.isEmpty()){
            progressBar.setVisibility(View.VISIBLE);


            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterDriver.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        String id=databaseReference.push().getKey();
                        Driver driver=new Driver();
                        driver.setName(name);
                        driver.setEmail(email);
                        driver.setPhone(phone);
                        driver.setPassword(password);
                        driver.setCarType(car_type);
                        driver.setId(id);

                        databaseReference.child(id).setValue(driver);

                        startActivity(new Intent(getApplication(),MainActivity.class));
                        finish();



                    }
                    else {
                        Snackbar.make(register_driver,"بريد إلكتروني غير صحيح أو كلمة سر ضعيفة",Snackbar.LENGTH_LONG).show();

                        progressBar.setVisibility(View.GONE);


                    }
                }

            });

        }
        else if (name.isEmpty()&&email.isEmpty()&&phone.isEmpty()&&password.isEmpty()&&car_type.isEmpty()){
            Snackbar.make(register_driver,"الحقول فارغة ",Snackbar.LENGTH_SHORT).show();

            TextUtils.isEmpty(name);
            ed_name.setError("أدخل اسمك ");
            TextUtils.isEmpty(email);
            ed_email.setError("يجب إدخال بريد إلكتروني");
            TextUtils.isEmpty(phone);
            ed_phone.setError("يجب إدخال رقم الهاتف");
            TextUtils.isEmpty(password);
            ed_password.setError("أدخل كلمة المرور ");
            TextUtils.isEmpty(car_type);
            ed_password.setError("أدخل نوع السيارة  ");
            return;
        }
        else if (email.isEmpty()&&phone.isEmpty()&password.isEmpty()&&car_type.isEmpty()){
            TextUtils.isEmpty(email);
            ed_email.setError("يجب إدخال بريد إلكتروني");
            TextUtils.isEmpty(phone);
            ed_phone.setError("يجب إدخال رقم الهاتف");
            TextUtils.isEmpty(password);
            ed_password.setError("أدخل كلمة المرور ");
            TextUtils.isEmpty(car_type);
            ed_password.setError("أدخل نوع السيارة  ");
            return;
        }
        else if (phone.isEmpty()&&password.isEmpty()&&car_type.isEmpty()){
            TextUtils.isEmpty(phone);
            ed_phone.setError("يجب إدخال رقم الهاتف");
            TextUtils.isEmpty(password);
            ed_password.setError("أدخل كلمة المرور ");
            TextUtils.isEmpty(car_type);
            ed_password.setError("أدخل نوع السيارة  ");
            return;
        }
        else if (password.isEmpty()&&car_type.isEmpty()){
            TextUtils.isEmpty(password);
            ed_password.setError("أدخل كلمة المرور ");
            TextUtils.isEmpty(car_type);
            ed_password.setError("أدخل نوع السيارة  ");
            return;
        }
        else if (car_type.isEmpty()) {
            TextUtils.isEmpty(car_type);
            ed_password.setError("أدخل نوع السيارة  ");
            return;
        }
    }
}
