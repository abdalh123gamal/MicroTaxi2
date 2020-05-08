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
import com.abdalh.microtaxi.model.Rider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;

public class RegisterRider extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    ProgressDialog pd;
    EditText ed_email,ed_name,ed_phone,ed_password;
    Button btn_register;
    ConstraintLayout register_rider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_rider);
        register_rider=findViewById(R.id.register_rider);


        ed_name=findViewById(R.id.register_rider_ed_name);
        ed_email=findViewById(R.id.register_rider_ed_email);
        ed_phone=findViewById(R.id.register_rider_ed_phone);
        ed_password=findViewById(R.id.register_rider_ed_password);
        btn_register=findViewById(R.id.register_rider_btn_register);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             registerRider();

                }






            });
        }
        public void registerRider(){
//           pd = new ProgressDialog(RegisterRider.this);
//            pd.setMessage("جاري إنشاء حساب");
            final AlertDialog dialog = new SpotsDialog(RegisterRider.this,"جاري إنشاء حساب راكب ..",R.style.CustomDialog);

            dialog.show();

            auth=FirebaseAuth.getInstance();
            firebaseDatabase=FirebaseDatabase.getInstance();
            databaseReference=firebaseDatabase.getReference().child("Users").child("Rider");

            final String name =ed_name.getText().toString();
            final String email =ed_email.getText().toString();
            final String phone =ed_phone.getText().toString();
            final String password =ed_password.getText().toString();
            if(!name.isEmpty()&&!email.isEmpty()&&!phone.isEmpty()&&!password.isEmpty()){




                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterRider.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            String id=auth.getCurrentUser().getUid();
                            Rider rider=new Rider();
                            rider.setName(name);
                            rider.setEmail(email);
                            rider.setPhone(phone);
                            rider.setPassword(password);
                            rider.setId(id);

                            databaseReference.child(id).setValue(rider);

                            startActivity(new Intent(getApplication(), RiderHome.class));
                            finish();



                        }
                        else {
                            dialog.dismiss();
                            Snackbar.make(register_rider,"بريد إلكتروني غير صحيح أو كلمة سر ضعيفة",Snackbar.LENGTH_LONG).show();



                        }
                    }

                });





            }
            else if (name.isEmpty()&&email.isEmpty()&&phone.isEmpty()&&password.isEmpty()){
                Snackbar.make(register_rider,"الحقول فارغة ",Snackbar.LENGTH_SHORT).show();

                TextUtils.isEmpty(name);
                ed_name.setError("أدخل اسمك ");
                TextUtils.isEmpty(email);
                ed_email.setError("يجب إدخال بريد إلكتروني");
                TextUtils.isEmpty(phone);
                ed_phone.setError("يجب إدخال رقم الهاتف");
                TextUtils.isEmpty(password);
                ed_password.setError("أدخل كلمة المرور ");
                return;
            }
            else if (email.isEmpty()&&phone.isEmpty()&password.isEmpty()){
                TextUtils.isEmpty(email);
                ed_email.setError("يجب إدخال بريد إلكتروني");
                TextUtils.isEmpty(phone);
                ed_phone.setError("يجب إدخال رقم الهاتف");
                TextUtils.isEmpty(password);
                ed_password.setError("أدخل كلمة المرور ");
                return;
            }
            else if (phone.isEmpty()&&password.isEmpty()){
                TextUtils.isEmpty(phone);
                ed_phone.setError("يجب إدخال رقم الهاتف");
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

