package com.abdalh.microtaxi.ui.HistoryRecycleView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.abdalh.microtaxi.R;
import com.abdalh.microtaxi.ui.HistorySingleActivity;

public class HistoryRecycleViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {


    public TextView rideId;
    public TextView time;
    public HistoryRecycleViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = itemView.findViewById(R.id.rideId);
        time = itemView.findViewById(R.id.time_ride);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);
    }
}



