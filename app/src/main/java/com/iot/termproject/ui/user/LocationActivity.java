package com.iot.termproject.ui.user;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonObject;
import com.iot.termproject.ApplicationClass;
import com.iot.termproject.ScanResultView;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.core.Server;
import com.iot.termproject.core.WifiService;
import com.iot.termproject.data.entity.LocationDistance;
import com.iot.termproject.data.entity.LocationWithNearbyPlaces;
import com.iot.termproject.data.entity.WifiData;
import com.iot.termproject.data.remote.Result;
import com.iot.termproject.data.remote.ScanResultService;
import com.iot.termproject.databinding.ActivityUserMainBinding;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 사용자 모드 (user mode)
 * 내 위치를 추적해 어디에 있는지를 알려주는 화면
 */
public class LocationActivity extends BaseActivity<ActivityUserMainBinding> {
    private ScanResultService scanResultService = new ScanResultService();

    // Wi-Fi
    private WifiData mWifiData;
    private MainActivityReceiver mReceiver = new MainActivityReceiver();
    private Intent wifiService;

    // gps
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private LocationManager manager;
    private GPSListener gpsListener;

    // ViewBinding
    @Override
    protected ActivityUserMainBinding setViewBinding() {
        return ActivityUserMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initAfterBinding() {
        mWifiData = null;

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        // check 버튼 클릭 시
        binding.mainCheckBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startLocationService();
            }
        });
    }

    private void startLocationService() {
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            gpsListener = new GPSListener();
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, gpsListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mWifiData;
    }

    // BroadcastReceiver: 관심 있는 이벤트가 발생할 때 브로드캐스트가 전송되는데, 단말기 안에서 이루어지는 수많은 일들을 대신해서 알려준다.
    public class MainActivityReceiver extends BroadcastReceiver implements ScanResultView {

        @Override
        public void onReceive(Context context, Intent intent) {
            mWifiData = (WifiData) intent.getParcelableExtra("WIFI_DATA");

            if (mWifiData != null) {
                // 사용자의 위치 정보를 받아온다.
                // 1: default
                JSONObject jsonObject = Server.processingAlgorithms(mWifiData.getmNetworks(), getApplicationContext(), latitude, longitude);
//                Log.d(TAG, String.valueOf(jsonArray));

                // TODO: JSON data를 siwoosiwoo.com:5000 전송
                // TODO: siwoosiwoo.com:5000로부터 강의실을 받아오기

                scanResultService.scanResult(this, jsonObject);
                stopService(wifiService);

//                if (userLocation == null) {
//                    // 사용자로부터 받아온 위치 정보가 없을 경우
//                    binding.mainAnswerTv.setText("None");
//                } else {
//                    // 사용자로부터 받아온 위치 정보가 있을 경우
//                    LocationDistance theNearestPoint = ApplicationClass.getTheNearestPoint(userLocation);
//                    if (theNearestPoint != null) {
//                        binding.mainAnswerTv.setText(theNearestPoint.getName() + "호");
//                    }
//                }
            }
        }

        @Override
        public void onScanResultSuccess(int referencePoint) {
            Log.d(TAG, "result: " + referencePoint);
            binding.mainAnswerTv.setText(referencePoint);
        }

        @Override
        public void onScanResultFailure() {
            Log.d(TAG, "onScanResultFailure()");
            Toast.makeText(getApplicationContext(), "응답 실패", Toast.LENGTH_SHORT).show();
        }
    }

    // 생명주기 - onDestroy()
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopService(wifiService);
    }

    class GPSListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.d(TAG, "onLocationChanged");

            latitude = location.getLatitude();
            longitude = location.getLongitude();

            // set receiver
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, new IntentFilter("ANDROID_WIFI_SCANNER"));

            // start Wi-Fi service
            wifiService = new Intent(getApplicationContext(), WifiService.class);
            startService(wifiService);

            // recover ratained object
            mWifiData = (WifiData) getLastNonConfigurationInstance();

            manager.removeUpdates(gpsListener);
        }
    }
}
