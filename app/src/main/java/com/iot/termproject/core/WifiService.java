package com.iot.termproject.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.iot.termproject.data.entity.WifiData;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Wi-Fi service
 */
public class WifiService extends Service {
    private static final String TAG = "SERVICE/WIFI";

    // Wi-Fi
    private WifiData mWifiData;
    private WifiManager mWifiManager;
    private ScheduledFuture<?> mScheduleReaderHandle;
    private ScheduledExecutorService mScheduler;

    private long initialDelay = 0;
    //    private long periodReader = 3000;
    private long periodReader = 1000;

    // 주기적으로 사용 가능한 와이파이 네트워크를 스캔하는 스레드를 생성해준다.
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        mWifiData = new WifiData();
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mScheduler = Executors.newScheduledThreadPool(1);
        mScheduleReaderHandle = mScheduler.scheduleAtFixedRate(new ScheduleReader(), initialDelay, periodReader, TimeUnit.MILLISECONDS);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // stop read thread
        mScheduleReaderHandle.cancel(true);
        mScheduler.shutdown();
        super.onDestroy();
    }

    // 와이파이 검색 결과를 주기적으로 읽은 다음 네트워크 목록을 포함하는
    // 새 WifiData 개체를 만들고 마지막으로 표시하기 위해 기본 액티비티로 보낸다.
    class ScheduleReader implements Runnable {

        @Override
        public void run() {
            if (mWifiManager.isWifiEnabled()) {
                // get networks
                // WifiManager로부터 스캔 리스트를 받아서 저장한 다음,
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                Log.d(TAG, "ScheduleReader/run/scanResults: " + scanResults);

                // store networks
                // WifiData에 network list 추가
                mWifiData.addNetworkList(scanResults);

                // send data to UI
                // UI로 데이터를 보내준다.
                Intent intent = new Intent("ANDROID_WIFI_SCANNER");
                intent.putExtra("WIFI_DATA", mWifiData);
                LocalBroadcastManager.getInstance(WifiService.this).sendBroadcast(intent);
            }
        }
    }
}
