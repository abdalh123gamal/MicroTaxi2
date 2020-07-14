package com.abdalh.microtaxi.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.LinearLayout;

import com.abdalh.microtaxi.R;
import com.abdalh.microtaxi.ui.HistoryRecycleView.HistoryAdapter;
import com.abdalh.microtaxi.ui.HistoryRecycleView.HistoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView mHistoryRecycleView;
    private HistoryAdapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;
    private String RiderOrDriver,userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mHistoryRecycleView=findViewById(R.id.history_recycleView);

        mHistoryLayoutManager= new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecycleView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter =new HistoryAdapter(getItemList(),this);
        mHistoryRecycleView.setAdapter(mHistoryAdapter);
        mHistoryRecycleView.setNestedScrollingEnabled(false);
        mHistoryRecycleView.setHasFixedSize(true);

        RiderOrDriver=getIntent().getExtras().getString("RiderOrDriver");
        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();
    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(RiderOrDriver).child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history: dataSnapshot.getChildren()){
                        FetchRideInformation(history.getKey());

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String rideId = dataSnapshot.getKey();
                    Long timestamp = 0L;
                    String distance = "";
                    Double ridePrice = 0.0;

                    if(dataSnapshot.child("timestamp").getValue() != null){
                        timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                    }
                    HistoryModel model=new HistoryModel(rideId,getDate(timestamp));
                    arrayList.add(model);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("MM/dd/yyyy EEE hh:mm a", cal).toString();
        return date;
    }
    ArrayList<HistoryModel> arrayList=new ArrayList<>();
    private ArrayList<HistoryModel> getItemList(){
        return arrayList;

    }


}
