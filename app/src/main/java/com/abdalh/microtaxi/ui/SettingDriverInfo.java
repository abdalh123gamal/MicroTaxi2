package com.abdalh.microtaxi.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.abdalh.microtaxi.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingDriverInfo extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private ImageView iv_driver;

    private EditText ed_name,ed_email,ed_phone,ed_password,ed_carType;
    Button btn_edit_info,btn_back;

    private String driverID;

    private String mName;
    private String mEmail;
    private String mPhone;
    private String mPassword;
    private String mCarType;
    private String mProfileImageUrl;

    private int PICK_IMAGE_PROFILE_REQUEST=2;
    private Uri resultUriImageProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_driver_info);

        iv_driver=findViewById(R.id.setting_driver_iv_driver);

        ed_name=findViewById(R.id.setting_driver_ed_name);
        ed_email=findViewById(R.id.setting_driver_ed_email);
        ed_phone=findViewById(R.id.setting_driver_ed_phone);
        ed_password=findViewById(R.id.setting_driver_ed_password);
        ed_carType=findViewById(R.id.setting_driver_ed_car_type);

        btn_edit_info=findViewById(R.id.setting_driver_btn_edit);
        btn_back=findViewById(R.id.setting_driver_btn_back);

        mAuth=FirebaseAuth.getInstance();
        driverID=mAuth.getCurrentUser().getUid();
        mDriverDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverID);
        getDriverInfo();

        btn_edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editInfoRider();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

        iv_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,PICK_IMAGE_PROFILE_REQUEST);
            }
        });


    }

    private void getDriverInfo(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map=(Map <String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName=map.get("name").toString();
                        ed_name.setText(mName);
                    }
                    if(map.get("email")!=null){
                        mEmail=map.get("email").toString();
                        ed_email.setText(mEmail);
                    }
                    if(map.get("phone")!=null){
                        mPhone=map.get("phone").toString();
                        ed_phone.setText(mPhone);
                    }
                    if(map.get("password")!=null){
                        mPassword=map.get("password").toString();
                        ed_password.setText(mPassword);
                    }
                    if(map.get("carType")!=null){
                        mCarType=map.get("carType").toString();
                        ed_carType.setText(mCarType);
                    }
                    if(map.get("profileImageUri")!=null){
                        mProfileImageUrl=map.get("profileImageUri").toString();
                        iv_driver.setImageURI(Uri.parse(mProfileImageUrl));
                        Glide.with(getApplicationContext()).load(mProfileImageUrl)
                                .placeholder(R.drawable.ic_waiting)
                                .into(iv_driver);
                    }
             }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void editInfoRider() {
//                String name=ed_name.getText().toString();
//                String email=ed_email.getText().toString();
//                String phone=ed_phone.getText().toString();
//                String password=ed_password.getText().toString();
//                Rider rider=new Rider(name,email,phone,password);
//                rider.setId(userID);
//                mRiderDatabase.setValue(rider);
//                Toast.makeText(getApplication(), "عدلتلك البيانات يا سيدي أما نشوف أخرتها  ", Toast.LENGTH_SHORT).show();


        mName=ed_name.getText().toString();
        mEmail=ed_email.getText().toString();
        mPhone =ed_phone.getText().toString();
        mPassword=ed_password.getText().toString();
        mCarType=ed_carType.getText().toString();

        Map driverInfo=new HashMap();
        driverInfo.put("name",mName);
        driverInfo.put("email",mEmail);
        driverInfo.put("phone",mPhone);
        driverInfo.put("password",mPassword);
        driverInfo.put("carType",mCarType);
        mDriverDatabase.updateChildren(driverInfo);

        if(resultUriImageProfile!=null){
            StorageReference filePath= FirebaseStorage.getInstance().getReference().child("profileImageUri").child(driverID);
            Bitmap bitmap=null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),resultUriImageProfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos= new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
            byte [] date  =baos.toByteArray();
            UploadTask uploadTask=filePath.putBytes(date);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String photoLink = uri.toString();
                            Map newImage= new HashMap();
                            newImage.put("profileImageUri",photoLink);
                            mDriverDatabase.updateChildren(newImage);

                        }
                    });



                }
            });
        }

        Toast.makeText(getApplication(), "عدلتلك البيانات يا سيدي أما نشوف أخرتها  ", Toast.LENGTH_SHORT).show();
        finish();
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_PROFILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            final Uri uriImageProfile = data.getData();
            resultUriImageProfile =uriImageProfile;
            iv_driver.setImageURI(resultUriImageProfile);
        }
    }
}
