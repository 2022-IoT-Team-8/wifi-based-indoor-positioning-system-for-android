package com.iot.termproject.user;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

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
import com.iot.termproject.databinding.ActivityUserMainBinding;

/**
 * 내 위치를 추적해 어디에 있는지를 알려주는 화면
 *
 * @see MainActivity 로부터 넘어온다.
 * @see NearbyAccessPointRVAdapter
 */
public class LocationActivity extends BaseActivity<ActivityUserMainBinding> {

    // Wi-Fi
    private WifiData mWifiData;
    private MainActivityReceiver mReceiver = new MainActivityReceiver();
    private Intent wifiService;

    // ViewBinding
    @Override
    protected ActivityUserMainBinding setViewBinding() {
        return ActivityUserMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initAfterBinding() {
        mWifiData = null;

        // check 버튼 클릭 시
        binding.mainCheckBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Set receiver
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, new IntentFilter("ANDROID_WIFI_SCANNER"));

                // Launch Wi-Fi service
                wifiService = new Intent(getApplicationContext(), WifiService.class);
                startService(wifiService);

                // Recover ratained object
                mWifiData = (WifiData) getLastNonConfigurationInstance();
            }
        });
    }

    public Object onRetainCustomNonConfigurationInstance() { return mWifiData; }

    // BroadcastReceiver: 관심 있는 이벤트가 발생할 때 브로드캐스트가 전송되는데, 단말기 안에서 이루어지는 수많은 일들을 대신해서 알려준다.
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mWifiData = (WifiData) intent.getParcelableExtra("WIFI_DATA");

            if(mWifiData != null) {
                // 사용자의 위치 정보를 받아온다.
                // 1: default
                LocationWithNearbyPlaces userLocation = Algorithm.processingAlgorithms(mWifiData.getmNetworks(), 1, getApplicationContext());

                if(userLocation == null) {
                    // 사용자로부터 받아온 위치 정보가 없을 경우
                    binding.mainAnswerTv.setText("None");
                } else {
                    LocationDistance theNearestPoint = ApplicationClass.getTheNearestPoint(userLocation);
                    if(theNearestPoint != null) {
                        binding.mainAnswerTv.setText(theNearestPoint.getName() + "호");
                    }
                }
            }
        }
    }

    // 생명주기 - onDestroy()
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopService(wifiService);
    }
}
