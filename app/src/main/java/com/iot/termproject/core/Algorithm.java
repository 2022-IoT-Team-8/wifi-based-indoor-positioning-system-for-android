package com.iot.termproject.core;

import android.content.Context;
import android.util.Log;

import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.entity.LocationWithNearbyPlaces;
import com.iot.termproject.data.entity.WifiDataNetwork;
import com.iot.termproject.data.entity.AccessPoint;

import java.util.ArrayList;
import java.util.List;

public class Algorithm {
    private static final String TAG = "ALGORITHM";

    /**
     * Location with nearby places
     *
     * @param scans    현재 scan된 access point들의 목록
     * @param mContext context
     * @return 사용자의 위치를 반환한다.
     */
    public static LocationWithNearbyPlaces processingAlgorithms(List<WifiDataNetwork> scans, Context mContext) {
        AppDatabase mRoom = AppDatabase.Companion.getInstance(mContext);
        assert mRoom != null;
        ArrayList<AccessPoint> accessPoints = (ArrayList<AccessPoint>) mRoom.accessPointDao().getAll();
        ArrayList<Float> observedRSSValues = new ArrayList<>();

        WifiDataNetwork wifiDataNetwork;
        int notFoundCounter = 0;

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
                observedRSSValues.add(-110.0f);
                ++notFoundCounter;
            }

            //Todo: 반복문을 도는 동안 MAC과 RSSI값을 JsonArray에 넣기
            /**
             * < 형식 >
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

            Log.d(TAG, observedRSSValues.get(i).toString() + "\n");
        }

        // TODO: JSON data를 siwoosiwoo.com:5000 전송

        if (notFoundCounter == accessPoints.size()) {
            return null;
        }

        // TODO: siwoosiwoo.com:5000로부터 강의실을 받아오기
        return null;
    }
}
