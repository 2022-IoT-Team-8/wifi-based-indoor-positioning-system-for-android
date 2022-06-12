package com.iot.termproject.core;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.iot.termproject.ScanResultView;
import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.entity.LocationWithNearbyPlaces;
import com.iot.termproject.data.entity.WifiDataNetwork;
import com.iot.termproject.data.entity.AccessPoint;
import com.iot.termproject.data.remote.Result;
import com.iot.termproject.data.remote.ScanResultService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final String TAG = "SERVER";

    public static JsonObject processingData(List<WifiDataNetwork> scans, Context mContext, Double latitude, Double longitude) {
        AppDatabase mRoom = AppDatabase.Companion.getInstance(mContext);
        assert mRoom != null;
        ArrayList<AccessPoint> accessPoints = (ArrayList<AccessPoint>) mRoom.accessPointDao().getAll();
        ArrayList<Float> observedRSSValues = new ArrayList<>();

        WifiDataNetwork wifiDataNetwork;

        JSONObject jsonObject = new JSONObject();
        JsonObject data = new JsonObject();

        int i = 0, j = 0;
        for (i = 0; i < accessPoints.size(); i++) {
            for (j = 0; j < scans.size(); j++) {
                wifiDataNetwork = scans.get(j);

                // 가지고 있는 access point 중에 내가 받은 access point의 mac address가 있는지 확인한다.
                if (accessPoints.get(i).getMacAddress().compareTo(wifiDataNetwork.getBssid()) == 0) {
                    observedRSSValues.add((float) wifiDataNetwork.getLevel());
                    break;
                }
            }

            // 너무 작은 신호의 세기를 가지고 있는 경우, 최솟값을 부여한다.
            if (j == scans.size()) {
                observedRSSValues.add(110.0f);
            }
        }

        try{

            for(i = 0; i < accessPoints.size(); i++){
                jsonObject.put(accessPoints.get(i).getMacAddress(), observedRSSValues.get(i));
                data.addProperty(accessPoints.get(i).getMacAddress(), observedRSSValues.get(i));
            }

            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);

            data.addProperty("latitude", latitude);
            data.addProperty("longitude", longitude);

        }catch(Exception e){
            e.printStackTrace();
        }

        return data;
    }
}
