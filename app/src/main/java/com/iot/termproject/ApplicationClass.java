package com.iot.termproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;

import com.iot.termproject.data.entity.LocationDistance;
import com.iot.termproject.data.entity.LocationWithNearbyPlaces;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

public class ApplicationClass {
    // RSSI value for no reception
    public static final Float NaN = -110.0f;
    public static final String SERVER_URL = "";

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // GPS가 가능한지 아닌지에 대한 값을 return 해준다.
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // 위치 정보를 받아와 형식에 맞게 쪼개준다. (위도, 경도 형식)
    public static String reduceDecimalPlaces(String location) {
        NumberFormat formatter = new DecimalFormat("#0.00");
        String[] split = location.split(" ");
        Double latValue = Double.valueOf(split[0]);
        Double lonValue = Double.valueOf(split[1]);
        String latFormat = formatter.format(latValue);  // 위도
        String lonFormat = formatter.format(lonValue);  // 경도
        return latFormat + ", " + lonFormat;
    }

    // 위치 정보를 받아와 알맞게 계산해준 값을 반환한다.
    public static String getTheDistancefromOrigin(String location) {
        NumberFormat formatter = new DecimalFormat("#0.00");
        String[] split = location.split(" ");
        Double latValue = Double.valueOf(split[0]);
        Double lonValue = Double.valueOf(split[1]);
        double distance = Math.sqrt(latValue * latValue + lonValue * lonValue);
        return formatter.format(distance);
    }

    // 제일 가까운 위치 정보를 찾아준다.
    public static LocationDistance getTheNearestPoint(LocationWithNearbyPlaces loc) {
        ArrayList<LocationDistance> places = loc.getPlaces();
        if (places != null && places.size() > 0) {
            Collections.sort(places);
            return places.get(0);
        }
        return null;
    }
}
