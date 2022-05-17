package com.iot.termproject.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.iot.termproject.R;
import com.iot.termproject.admin.data.Floor;
import com.iot.termproject.databinding.ItemIntroBinding;

import java.util.ArrayList;

public class IntroAdapter extends BaseAdapter {
    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<Floor> floors;

    public IntroAdapter(Context mContext, ArrayList<Floor> floors) {
        mLayoutInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
        this.floors = floors;
    }

    @Override
    public int getCount() {
        return floors.size();
    }

    @Override
    public Floor getItem(int position) {
        return floors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View converView, ViewGroup viewGroup) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = mLayoutInflater.inflate(R.layout.item_intro, null);

        TextView floorTv = view.findViewById(R.id.item_intro_floor_tv);
        TextView roomTv = view.findViewById(R.id.item_intro_room_tv);

        String floor = floors.get(position).getFloor() + "F";
        String room = floors.get(position).getFloor() + "01호 ~ " + floors.get(position).getMaxRoomNumber() + "호";

        floorTv.setText(floor);
        roomTv.setText(room);

        return view;
    }
}
