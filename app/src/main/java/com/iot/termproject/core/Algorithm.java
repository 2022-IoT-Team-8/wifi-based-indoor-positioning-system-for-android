package com.iot.termproject.core;

import android.content.Context;

import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.LocationDistance;
import com.iot.termproject.data.LocationWithNearbyPlaces;
import com.iot.termproject.data.WifiDataNetwork;
import com.iot.termproject.data.entity.AccessPoint;
import com.iot.termproject.data.entity.RoomPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 'LocateMeActivity'를 위한 알고리즘들
 * <p>
 * 1. KNN_WKNN
 * 2. KNN_WKNN (with weight)
 * 3. MAP_MMSE
 * 4. MAP_MMSE (with weight)
 */
public class Algorithm {
    private AppDatabase database;
    final static String K = "4";

    /**
     * Location with nearby places
     * @param scans 현재 scan된 access point들의 목록
     * @param choice 선택한 알고리즘을 나타낸다.
     * @param mContext context
     * @return 사용자의 위치를 반환한다.
     */
    public static LocationWithNearbyPlaces processingAlgorithms(List<WifiDataNetwork> scans, int choice, Context mContext) {
        AppDatabase database = AppDatabase.Companion.getInstance(mContext);
        assert database != null;
        ArrayList<AccessPoint> accessPoints = (ArrayList<AccessPoint>) database.accessPointDao().getAll();
        ArrayList<Float> observedRSSValues = new ArrayList<>();

        WifiDataNetwork wifiDataNetwork;
        int notFoundCounter = 0;

        int i = 0, j = 0;
        for (i = 0; i < accessPoints.size(); ++i) {
            for (j = 0; j < scans.size(); ++j) {
                wifiDataNetwork = scans.get(j);

                // 가지고 있는 access point 중에 내가 받은 access point의 mac address가 있는지 확인한다.
                if (accessPoints.get(i).getMac_address().compareTo(wifiDataNetwork.getBssid()) == 0) {
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

        String parameter = readParameter(choice);

        if (parameter == null) {
            return null;
        }

        switch (choice) {
            case 1:
                return KNN_WKNN_Algorithm(mContext, observedRSSValues, parameter, false);
            case 2:
                return KNN_WKNN_Algorithm(mContext, observedRSSValues, parameter, true);
            case 3:
                return MAP_MMSE_Algorithm(mContext, observedRSSValues, parameter, false);
            case 4:
                return MAP_MMSE_Algorithm(mContext, observedRSSValues, parameter, true);
        }
        return null;
    }

    /**
     * KNN 알고리즘
     * 사용자의 위치를 KNN 알고리즘을 이용해서 계산한다.
     *
     * @param mContext Context
     * @param observedRSSValues 현재 관찰된 RSS value 값들
     * @param parameter K 값
     * @param isWeighted 가중치를 적용할지 안 할지에 대한 것
     * @return 추정된 사용자의 위치 정보
     */
    private static LocationWithNearbyPlaces KNN_WKNN_Algorithm(Context mContext, ArrayList<Float> observedRSSValues, String parameter, boolean isWeighted) {
        AppDatabase database = AppDatabase.Companion.getInstance(mContext);
        ArrayList<LocationDistance> locationDistanceResults = new ArrayList<>();
        ArrayList<AccessPoint> rssValues = new ArrayList<>();

        int K;
        double currentResult = 0;
        String mLocation = null;

        // K 값을 문자열에서 정수화 시킨다.
        try {
            K = Integer.parseInt(parameter);
        } catch (Exception e) {
            return null;
        }

        // Construct a list with locations-distances pairs for currently
        // observed RSS values
        assert database != null;
        for (RoomPoint roomPoint : database.roomPointDao().getAll()) {
            rssValues = (ArrayList<AccessPoint>) roomPoint.getAccessPointList();
            assert rssValues != null;
            currentResult = calculateEuclideanDistance(rssValues, observedRSSValues);

            if (currentResult == Float.NEGATIVE_INFINITY)
                return null;

            locationDistanceResults.add(0,
                    new LocationDistance(currentResult, roomPoint.getLocationId(), roomPoint.getName()));
        }

        // Sort locations-distances pairs based on minimum distances
        Collections.sort(locationDistanceResults, new Comparator<LocationDistance>() {
            public int compare(LocationDistance gd1, LocationDistance gd2) {
                return (Double.compare(gd1.getDistance(), gd2.getDistance()));
            }
        });

        if (!isWeighted) {
            mLocation = calculateAverageKDistanceLocations(locationDistanceResults, K);
        } else {
            mLocation = calculateWeightedAverageKDistanceLocations(locationDistanceResults, K);
        }

        return new LocationWithNearbyPlaces(mLocation, locationDistanceResults);
    }

    /**
     * (MAP) 알고리즘 or Probabilistic Minimum Mean Square Error (MMSE)
     * Calculates user location based on Probabilistic Maximum A Posteriori
     *
     * @param mContext Context
     * @param observedRssValues 현재 관찰된 RSS value 값들
     * @param parameter K 값
     * @param isWeighted 가중치를 적용할지 안 할지에 대한 것
     * @return 추정된 사용자의 위치 정보
     */
    private static LocationWithNearbyPlaces MAP_MMSE_Algorithm(Context mContext, ArrayList<Float> observedRssValues, String parameter, boolean isWeighted) {
        AppDatabase database = AppDatabase.Companion.getInstance(mContext);
        ArrayList<LocationDistance> locationDistanceResults = new ArrayList<>();
        ArrayList<AccessPoint> rssValues = new ArrayList<>();

        float sGreek;
        double currentResult = 0.0d;
        double highestProbability = Double.NEGATIVE_INFINITY;
        String mLocation = null;

        try {
            sGreek = Float.parseFloat(parameter);
        } catch (Exception e) {
            return null;
        }

        // Find the location of user with the highest probability
        assert database != null;
        for (RoomPoint roomPoint : database.roomPointDao().getAll()) {
            rssValues = (ArrayList<AccessPoint>) roomPoint.getAccessPointList();
            currentResult = calculateProbability(rssValues, observedRssValues, sGreek);

            if (currentResult == Double.NEGATIVE_INFINITY)
                return null;
            else if (currentResult > highestProbability) {
                highestProbability = currentResult;
                mLocation = roomPoint.getLocationId();
            }

            if (isWeighted)
                locationDistanceResults.add(0,
                        new LocationDistance(currentResult, roomPoint.getLocationId(), roomPoint.getName()));
        }

        if (isWeighted)
            mLocation = calculateWeightedAverageProbabilityLocations(locationDistanceResults);

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
                return Float.NEGATIVE_INFINITY;
            }

            temp = originalMeanRss - observedMeanRss;
            temp *= temp;
            finalResult += temp;
        }
        return Math.sqrt(finalResult);
    }

    /**
     * 확률 계산
     * 기존 데이터베이스에 저장된 특정 위치에 대한 RSS 값과 현재 관찰된 RSS 값을 비교한다.
     *
     * @param original
     * @param observed
     * @param sGreek
     * @return
     */
    private static double calculateProbability(ArrayList<AccessPoint> original, ArrayList<Float> observed, float sGreek) {
        double finalResult = 1;
        double v1, v2, temp;

        for (int i = 0; i < original.size(); ++i) {
            try {
                v1 = original.get(i).getMeanRss();
                v2 = observed.get(i);
            } catch (Exception e) {
                return Double.NEGATIVE_INFINITY;
            }

            temp = v1 - v2;
            temp *= temp;
            temp = -temp;
            temp /= (double) (sGreek * sGreek);
            temp = Math.exp(temp);

            // Do not allow zero instead stop on small possibility
            if (finalResult * temp != 0)
                finalResult = finalResult * temp;
        }
        return finalResult;
    }

    /**
     * 가장 짧은 거리 D를 가진 K 위치들의 평균을 계산한다.
     *
     * @param locationDistances 거리로 정렬되어 있는 Location-Distance 맵
     * @param K 사용되는 위치 개수
     * @return 추정된 사용자의 위치 정보 혹은 에러가 발생한 경우 null
     */
    private static String calculateAverageKDistanceLocations(ArrayList<LocationDistance> locationDistances, int K) {
        float sumX = 0.0f;
        float sumY = 0.0f;

        String[] LocationArray = new String[2];
        float x, y;

        int K_Min = Math.min(K, locationDistances.size());

        // Calculate the sum of X and Y
        for (int i = 0; i < K_Min; ++i) {
            LocationArray = locationDistances.get(i).getLocation().split(" ");

            try {
                x = Float.parseFloat(LocationArray[0].trim());
                y = Float.parseFloat(LocationArray[1].trim());
            } catch (Exception e) {
                return null;
            }

            sumX += x;
            sumY += y;
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
     * @param K 사용되는 위치 개수
     * @return 추정된 사용자의 위치 정보 혹은 에러가 발생한 경우 null
     */
    private static String calculateWeightedAverageKDistanceLocations(ArrayList<LocationDistance> locationDistances, int K) {
        double LocationWeight = 0.0f;
        double sumWeights = 0.0f;
        double WeightedSumX = 0.0f;
        double WeightedSumY = 0.0f;

        String[] LocationArray = new String[2];
        float x, y;

        int K_Min = Math.min(K, locationDistances.size());

        // Calculate the weighted sum of X and Y
        for (int i = 0; i < K_Min; ++i) {
            if (locationDistances.get(i).getDistance() != 0.0) {
                LocationWeight = 1 / locationDistances.get(i).getDistance();
            } else {
                LocationWeight = 100;
            }
            LocationArray = locationDistances.get(i).getLocation().split(" ");

            try {
                x = Float.parseFloat(LocationArray[0].trim());
                y = Float.parseFloat(LocationArray[1].trim());
            } catch (Exception e) {
                return null;
            }

            sumWeights += LocationWeight;
            WeightedSumX += LocationWeight * x;
            WeightedSumY += LocationWeight * y;
        }

        WeightedSumX /= sumWeights;
        WeightedSumY /= sumWeights;

        return WeightedSumX + " " + WeightedSumY;
    }

    /**
     * Calculates the Weighted Average over ALL locations
     * where the weights are the Normalized Probabilities
     *
     * @param locationDistances 거리로 정렬되어 있는 Location-Distance 맵
     * @return 추정된 사용자의 위치 정보 혹은 에러가 발생한 경우 null
     */
    private static String calculateWeightedAverageProbabilityLocations(ArrayList<LocationDistance> locationDistances) {
        double sumProbabilities = 0.0f;
        double WeightedSumX = 0.0f;
        double WeightedSumY = 0.0f;
        double NP;
        float x, y;
        String[] LocationArray = new String[2];

        // Calculate the sum of all probabilities
        for (int i = 0; i < locationDistances.size(); ++i)
            sumProbabilities += locationDistances.get(i).getDistance();

        // Calculate the weighted (Normalized Probabilities) sum of X and Y
        for (int i = 0; i < locationDistances.size(); ++i) {
            LocationArray = locationDistances.get(i).getLocation().split(" ");

            try {
                x = Float.parseFloat(LocationArray[0].trim());
                y = Float.parseFloat(LocationArray[1].trim());
            } catch (Exception e) {
                return null;
            }

            NP = locationDistances.get(i).getDistance() / sumProbabilities;

            WeightedSumX += (x * NP);
            WeightedSumY += (y * NP);
        }

        return WeightedSumX + " " + WeightedSumY;
    }

    private static String readParameter(int choice) {
        String parameter = null;

        if (choice == 1) parameter = K;
        else if (choice == 2) parameter = K;
        else if (choice == 3) parameter = K;
        else if (choice == 4) parameter = K;

        return parameter;
    }
}
