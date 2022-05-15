package com.iot.termproject.user;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.iot.termproject.ApplicationClass;
import com.iot.termproject.adapter.NearbyAccessPointRVAdapter;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.core.Algorithm;
import com.iot.termproject.core.WifiService;
import com.iot.termproject.data.LocationDistance;
import com.iot.termproject.data.LocationWithNearbyPlaces;
import com.iot.termproject.data.WifiData;
import com.iot.termproject.databinding.ActivityLocateMeBinding;

/**
 * 내 위치를 추적해 어디에 있는지를 알려주는 화면
 *
 * @see MainActivity 로부터 넘어온다.
 * @see NearbyAccessPointRVAdapter
 */
public class LocateMeActivity extends BaseActivity<ActivityLocateMeBinding> {
    private static final String TAG = "ACT/LOCATE-ME";

    // Wi-Fi
    private WifiData mWifiData;
    private MainActivityReceiver mReceiver = new MainActivityReceiver();
    private Intent wifiService;

    // RecyclerView
    private NearbyAccessPointRVAdapter nearbyAccessPointRVAdapter = new NearbyAccessPointRVAdapter();

    // ViewBinding
    @Override
    protected ActivityLocateMeBinding setViewBinding() {
        return ActivityLocateMeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initAfterBinding() {
        mWifiData = null;

        // Set receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("ANDROID_WIFI_SCANNER"));

        // Launch Wi-Fi service
        wifiService = new Intent(this, WifiService.class);
        startService(wifiService);

        // Recover ratained object
        mWifiData = (WifiData) getLastNonConfigurationInstance();

        initRecyclerView();
    }

    public Object onRetainCustomNonConfigurationInstance() { return mWifiData; }

    // BroadcastReceiver: 관심 있는 이벤트가 발생할 때 브로드캐스트가 전송되는데, 단말기 안에서 이루어지는 수많은 일들을 대신해서 알려준다.
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "MainActivityReceiver/onReceive");

            mWifiData = (WifiData) intent.getParcelableExtra("WIFI_DATA");

            if(mWifiData != null) {
                // 사용자의 위치 정보를 받아온다.
                // 1: default
                LocationWithNearbyPlaces userLocation = Algorithm.processingAlgorithms(mWifiData.getmNetworks(), 1, getApplicationContext());
                Log.d(TAG, "MainActivityReceiver/onReceive/userLocation: " + userLocation);

                if(userLocation == null) {
                    // 사용자로부터 받아온 위치 정보가 없을 경우
                    binding.locateMeLocationTv.setText("Location: NA\nNote:Please switch on your wifi and location services with permission provided to App");
                } else {
                    // 위도, 경도 형식의 문자열로 바꿔준다.
                    String locationValue = ApplicationClass.reduceDecimalPlaces(userLocation.getLocation());
                    binding.locateMeLocationTv.setText("Location: " + locationValue);

                    // distance 계산
                    String distanceFromOrigin = ApplicationClass.getTheDistancefromOrigin(userLocation.getLocation());
                    binding.locateMeDistanceOriginTv.setText("The distance from stage area is: " + distanceFromOrigin + "m");

                    // 제일 가까운 point를 찾아준다.
                    LocationDistance theNearestPoint = ApplicationClass.getTheNearestPoint(userLocation);
                    if(theNearestPoint != null) {
                        binding.locateMeNearestLocationTv.setText("You are near to: " + theNearestPoint.getName());
                    }

                    // RecyclerView 초기화
                    initRecyclerView(userLocation);
                }
            }
        }
    }

    // RecyclerView 초기화
    private void initRecyclerView() {
        binding.locateMeNearbyPointsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.locateMeNearbyPointsRecyclerView.setAdapter(nearbyAccessPointRVAdapter);
    }

    // RecyclerView 초기화
    private void initRecyclerView(LocationWithNearbyPlaces userLocation) {
        binding.locateMeNearbyPointsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.locateMeNearbyPointsRecyclerView.setAdapter(nearbyAccessPointRVAdapter);
        nearbyAccessPointRVAdapter.addData(userLocation.getPlaces());
    }

    // 생명주기 - onDestroy()
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopService(wifiService);
    }
}
