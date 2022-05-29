package com.iot.termproject.core;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.entity.LocationDistance;
import com.iot.termproject.data.entity.LocationWithNearbyPlaces;
import com.iot.termproject.data.entity.WifiDataNetwork;
import com.iot.termproject.data.entity.AccessPoint;
import com.iot.termproject.data.entity.ReferencePoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 'LocateMeActivity'를 위한 알고리즘들
 * <p>
 * 1. KNN_WKNN
 * 2. KNN_WKNN (with weight)
 */
public class Algorithm {
    private static final String TAG = "ALGORITHM";
    private static final String K = "4";

    /**
     * Location with nearby places
     *
     * @param scans    현재 scan된 access point들의 목록
     * @param choice   선택한 알고리즘을 나타낸다.
     * @param mContext context
     * @return 사용자의 위치를 반환한다.
     */
    public static LocationWithNearbyPlaces processingAlgorithms(List<WifiDataNetwork> scans, int choice, Context mContext) {
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
        }

        if (notFoundCounter == accessPoints.size()) {
            return null;
        }

        switch (choice) {
            case 1:
                return KNN_WKNN_Algorithm(mContext, observedRSSValues, false);
            case 2:
                return KNN_WKNN_Algorithm(mContext, observedRSSValues, true);
        }
        return null;
    }

    /**
     * KNN 알고리즘
     * 사용자의 위치를 KNN 알고리즘을 이용해서 계산한다.
     *
     * @param mContext          Context
     * @param observedRSSValues 현재 관찰된 RSS value 값들
     * @param isWeighted        가중치를 적용할지 안 할 지에 대한 것
     * @return 추정된 사용자의 위치 정보
     */
    private static LocationWithNearbyPlaces KNN_WKNN_Algorithm(Context mContext, ArrayList<Float> observedRSSValues, boolean isWeighted) {
        AppDatabase mRoom = AppDatabase.Companion.getInstance(mContext);
        ArrayList<LocationDistance> locationDistanceResults = new ArrayList<>();
        ArrayList<AccessPoint> rssValues = new ArrayList<>();

        double currentResult = 0;
        String mLocation = null;

        // Construct a list with locations-distances pairs for currently
        // observed RSS values
        assert mRoom != null;
        for (ReferencePoint referencePoint : mRoom.referencePointDao().getAll()) {
            rssValues = (ArrayList<AccessPoint>) referencePoint.getAccessPoints();

            assert rssValues != null;
            currentResult = calculateEuclideanDistance(rssValues, observedRSSValues);

            if (currentResult == Float.NEGATIVE_INFINITY)
                return null;

            locationDistanceResults.add(0,
                    new LocationDistance(currentResult, referencePoint.getLatitude(), referencePoint.getLongitude(), String.valueOf(referencePoint.getName())));
        }

        // Sort locations-distances pairs based on minimum distances
        Collections.sort(locationDistanceResults, new Comparator<LocationDistance>() {
            public int compare(LocationDistance gd1, LocationDistance gd2) {
                return (Double.compare(gd1.getDistance(), gd2.getDistance()));
            }
        });

        if (!isWeighted) {
            mLocation = calculateAverageKDistanceLocations(locationDistanceResults, Integer.parseInt(K));
        } else {
            mLocation = calculateWeightedAverageKDistanceLocations(locationDistanceResults, Integer.parseInt(K));
        }

        return new LocationWithNearbyPlaces(mLocation, locationDistanceResults);
    }

    /**
     * Euclidean distance
     * 기존 데이터베이스에 저장된 특정 위치에 대한 RSS 값과 현재 관찰된 RSS 값을 비교한다.
     *
     * @param original 기존 데이터베이스에 저장된 RSS 값들
     * @param observed 현재 관찰된 RSS 값들
     * @return 계산 결과
     */
    private static double calculateEuclideanDistance(ArrayList<AccessPoint> original, ArrayList<Float> observed) {
        double finalResult = 0;
        double originalMeanRss, observedMeanRss, temp;

        for (int i = 0; i < original.size(); ++i) {
            try {
                original.get(i).getMeanRss();
                originalMeanRss = original.get(i).getMeanRss();
                observedMeanRss = observed.get(i);
            } catch (Exception e) {
                Log.d(TAG, "Exception");
                return Float.NEGATIVE_INFINITY;
            }

            temp = originalMeanRss - observedMeanRss;
            temp *= temp;
            finalResult += temp;
        }
        return Math.sqrt(finalResult);
    }

    /**
     * 가장 짧은 거리 D를 가진 K 위치들의 평균을 계산한다.
     *
     * @param locationDistances 거리로 정렬되어 있는 Location-Distance 맵
     * @param K                 사용되는 위치 개수
     * @return 추정된 사용자의 위치 정보 혹은 에러가 발생한 경우 null
     */
    private static String calculateAverageKDistanceLocations(ArrayList<LocationDistance> locationDistances, int K) {
        float sumX = 0.0f;
        float sumY = 0.0f;

        String[] LocationArray = new String[2];
        double latitude, longtidue;

        int K_Min = Math.min(K, locationDistances.size());

        // Calculate the sum of X and Y
        for (int i = 0; i < K_Min; ++i) {

            latitude = locationDistances.get(i).getLatitude();
            longtidue = locationDistances.get(i).getLongitude();

            sumX += latitude;
            sumY += longtidue;
        }

        // Calculate the average
        sumX /= K_Min;
        sumY /= K_Min;

        return sumX + " " + sumY;
    }

    /**
     * 가장 짧은 거리 D를 가진 K 위치들의 평균을 계산한다.
     * 가중치 존재
     *
     * @param locationDistances 거리로 정렬되어 있는 Location-Distance 맵
     * @param K                 사용되는 위치 개수
     * @return 추정된 사용자의 위치 정보 혹은 에러가 발생한 경우 null
     */
    private static String calculateWeightedAverageKDistanceLocations(ArrayList<LocationDistance> locationDistances, int K) {
        double LocationWeight = 0.0f;
        double sumWeights = 0.0f;
        double WeightedSumX = 0.0f;
        double WeightedSumY = 0.0f;

        String[] LocationArray = new String[2];
        double latitude;
        double longitude;

        int K_Min = Math.min(K, locationDistances.size());

        // Calculate the weighted sum of X and Y
        for (int i = 0; i < K_Min; ++i) {
            if (locationDistances.get(i).getDistance() != 0.0) {
                LocationWeight = 1 / locationDistances.get(i).getDistance();
            } else {
                LocationWeight = 100;
            }
            latitude = locationDistances.get(i).getLatitude();
            longitude = locationDistances.get(i).getLongitude();

            sumWeights += LocationWeight;
            WeightedSumX += LocationWeight * latitude;
            WeightedSumY += LocationWeight * longitude;
        }

        WeightedSumX /= sumWeights;
        WeightedSumY /= sumWeights;

        return WeightedSumX + " " + WeightedSumY;
    }
}
