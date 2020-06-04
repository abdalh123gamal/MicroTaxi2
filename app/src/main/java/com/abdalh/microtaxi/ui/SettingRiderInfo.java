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
import com.abdalh.microtaxi.model.Rider;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SettingRiderInfo extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mRiderDatabase;
    private ImageView iv_rider;
    private EditText ed_name,ed_email,ed_phone,ed_password;
    Button btn_edit_info,btn_back;

    private String userID;

    private String mName;
    private String mEmail;
    private String mPhone;
    private String mPassword;
    private String mProfileImageUrl;

    private int PICK_IMAGE_REQUEST=1;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_rider_info);

        iv_rider=findViewById(R.id.setting_rider_iv_rider);

        ed_name=findViewById(R.id.setting_rider_ed_name);
        ed_email=findViewById(R.id.setting_rider_ed_email);
        ed_phone=findViewById(R.id.setting_rider_ed_phone);
        ed_password=findViewById(R.id.setting_rider_ed_password);

        btn_edit_info=findViewById(R.id.setting_rider_btn_edit);
        btn_back=findViewById(R.id.setting_rider_btn_back);

        mAuth=FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();
        mRiderDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child(userID);
        getRiderInfo();

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

        iv_rider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,PICK_IMAGE_REQUEST);
            }
        });
    }


        private void getRiderInfo(){
        mRiderDatabase.addValueEventListener(new ValueEventListener() {
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
                    if(map.get("profileImageUri")!=null){
                        mProfileImageUrl=map.get("profileImageUri").toString();
                        iv_rider.setImageURI(Uri.parse(mProfileImageUrl));
                        Glide.with(getApplicationContext()).load(mProfileImageUrl)
                                .placeholder(R.drawable.ic_waiting)
                                .into(iv_rider);
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

            Map riderInfo=new HashMap();
            riderInfo.put("name",mName);
            riderInfo.put("email",mEmail);
            riderInfo.put("phone",mPhone);
            riderInfo.put("password",mPassword);
            mRiderDatabase.updateChildren(riderInfo);
            if(resultUri!=null){
                StorageReference filePath= FirebaseStorage.getInstance().getReference().child("profileImageUri").child(userID);
                Bitmap bitmap=null;
                try {
                    bitmap= MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),resultUri);
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
                                mRiderDatabase.updateChildren(newImage);

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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
           final Uri uriImage = data.getData();
           resultUri =uriImage;
           iv_rider.setImageURI(resultUri);
        }
    }
}
