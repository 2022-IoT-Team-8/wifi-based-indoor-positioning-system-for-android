package com.iot.termproject.core;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

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

    /**
     * Location with nearby places
     *
     * @param scans    현재 scan된 access point들의 목록
     * @param mContext context
     * @return 사용자의 위치를 반환한다.
     */
    public static JSONObject processingAlgorithms(List<WifiDataNetwork> scans, Context mContext, Double latitude, Double longitude) {
        AppDatabase mRoom = AppDatabase.Companion.getInstance(mContext);
        assert mRoom != null;
        ArrayList<AccessPoint> accessPoints = (ArrayList<AccessPoint>) mRoom.accessPointDao().getAll();
        ArrayList<Float> observedRSSValues = new ArrayList<>();

        WifiDataNetwork wifiDataNetwork;
        int notFoundCounter = 0;

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

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
                ++notFoundCounter;
            }

            // 필요할까?
//            Log.d(TAG, observedRSSValues.get(i).toString() + "\n");
        }

        try{

            /*
             * < Jsondata 형식 >
             *     {
             *         "Results":
             *                   [
             *                   { "MAC": "MAC Address", "RSSI", "RSSI 값" },
             *                   { "MAC": "MAC Address", "RSSI", "RSSI 값" },
             *                   { "MAC": "MAC Address", "RSSI", "RSSI 값" },
             *                   { "MAC": "MAC Address", "RSSI", "RSSI 값" },
             *                   { "MAC": "MAC Address", "RSSI", "RSSI 값" }
             *                   ]
             *     }
             */

            for(i = 0; i < accessPoints.size(); i++){

                // JsonObject 생성 - { "MAC": "MAC Address", "RSSI", "RSSI 값" }
//                JSONObject object = new JSONObject();
                jsonObject.put(accessPoints.get(i).getMacAddress(), observedRSSValues.get(i));
//                object.put("MAC", accessPoints.get(i).getMacAddress());
//                object.put("RSSI", accessPoints.get(i).getRssi());

                // JsonArray에 JsonObject 넣기
//                jsonArray.put(object);
            }

            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);

            Log.d(TAG, "SERVER/object: " + jsonObject);
            jsonArray.put(jsonObject);

            // Server에 전송할 json data 생성
            Log.d(TAG, "SERVER/jsonArray: " + jsonArray);
            jsonObject.put("Results", jsonArray);

        }catch(Exception e){
            e.printStackTrace();
        }

        return jsonObject;
    }
}
