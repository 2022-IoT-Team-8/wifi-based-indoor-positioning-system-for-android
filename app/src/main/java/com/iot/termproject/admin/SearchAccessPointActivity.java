package com.iot.termproject.admin;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.iot.termproject.adapter.WifiResultRVAdapter;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.entity.AccessPoint;
import com.iot.termproject.databinding.ActivitySearchAccessPointBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 'Select via Scan' 버튼 클릭 시 보여지는 화면
 * 주변 access point들의 정보를 읽어서 list 형태로 보여준다.
 * 이미 추가된 Mac address를 가진 AP의 경우 리스트에 뜨지 않도록 추가 구현해야 한다.
 *
 * @see AccessPointActivity 로부터 넘어온다.
 * @see WifiResultRVAdapter 스캔된 와이파이들의 결과를 보여주는 RecyclerView adapter
 */
public class SearchAccessPointActivity extends BaseActivity<ActivitySearchAccessPointBinding> {
    private static final String TAG = "ACT/SEARCH-AP";

    // database
    private AppDatabase mRoom;

    // wifi
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private List<ScanResult> wifiResults = new ArrayList<>();
    private boolean wasWifiEnabled;
    private WifiResultRVAdapter wifiResultRVAdapter;

    // thread
    private final Handler handler = new Handler();

    // ViewBinding 설정
    @Override
    protected ActivitySearchAccessPointBinding setViewBinding() {
        return ActivitySearchAccessPointBinding.inflate(getLayoutInflater());
    }

    // ViewBinding 이후 처리할 작업들을 넣어준다.
    // onCreate() 생명주기 이후
    @Override
    protected void initAfterBinding() {

        // ActionBar 수정
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setTitle("Search Wi-Fi");

        // RoomDB
        mRoom = AppDatabase.Companion.getInstance(this);

        // WifiManager로부터 객체를 받아와 저장한다.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // WifiReceiver (inner class) 객체 생성
        wifiReceiver = new WifiReceiver();

        // Wi-Fi를 받아오는 게 가능한지 체크
        wasWifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiManager.isWifiEnabled()) {
            // 만약 불가능하다면 true로 바꿔준다.
            wifiManager.setWifiEnabled(true);
        }
        Log.d(TAG, "initAfterBinding/wasWifiEnabled: " + wasWifiEnabled);

        // RecyclerView 초기화
        initRecyclerView();

        // Click listener 초기화
        initClickListener();
    }

    // RecyclerView 초기화
    private void initRecyclerView() {
        // view 설정
        binding.searchWifiApRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.searchWifiApRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // RecyclerView adapter 초기화
        wifiResultRVAdapter = new WifiResultRVAdapter(this, new WifiResultRVAdapter.MyItemClickListener() {

            // item 클릭한 경우 해당 acccess point에 대한 정보를 받아와 전달해준다.
            @Override
            public void onItemClick(@NonNull View view, int position) {
                ScanResult scanResult = wifiResults.get(position);

                // 해당 access point 객체 생성
                // FixMe: x, y 좌표를 어떻게 넣어줄 건지 고민해봐야 한다.
                AccessPoint accessPoint = new AccessPoint(scanResult.SSID, scanResult.BSSID, null);

                // Intent를 통해서 넘겨준다.
                Intent intent = new Intent();
                intent.putExtra("access_point", accessPoint);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        // RecyclerView adapter 연결
        binding.searchWifiApRecyclerView.setAdapter(wifiResultRVAdapter);
    }

    // Click listener
    private void initClickListener() {
        // 'refresh' 버튼 클릭 시 refresh()를 호출한다.
        binding.searchWifiApRefreshBtn.setOnClickListener(new View.OnClickListener() {

            // refresh
            @Override
            public void onClick(View view) {
                refresh();
            }
        });
    }

    // 생명주기 - onResume()
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        // 등록
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        refresh();
        super.onResume();
    }

    // 생명주기 - onPause()
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        // 해제
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    // Wi-Fi scan thread
    public void refresh() {
        Log.d(TAG, "refresh");

        // for test
        if (wifiManager.startScan()) {
            Toast.makeText(this, "Scanning Wi-Fi ...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to Scanning Wi-Fi ...", Toast.LENGTH_SHORT).show();
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Wi-Fi scan 시작
                Log.d(TAG, "refresh/run");
                wifiManager.startScan();
            }
        }, 1000);
    }

    // Wi-Fi list를 받는 역할
    // BoradcastReceiver 만들어서 onReceive 통해 주기적으로 Wi-Fi 신호를 받아온다.
    class WifiReceiver extends BroadcastReceiver {

        // 주기적으로 Wi-Fi 신호를 받아온다.
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            // ScanResults 통해서 얻은 Wi-Fi들을 저장한 다음에
            wifiResults = wifiManager.getScanResults();

            // 정렬해준다.
            Collections.sort(wifiResults, new Comparator<ScanResult>() {

                @Override
                public int compare(ScanResult scanResult1, ScanResult scanResult2) {

                    if (scanResult1.level > scanResult2.level) {
                        // scanResult2 should be before scanResult1
                        return -1;
                    }
                    else if (scanResult1.level < scanResult2.level) {
                        // ScanResult1 should be before ScanResult2
                        return 1;
                    }
                    // 둘 다 해당되지 않는 경우
                    return 0;
                }
            });

            Log.d(TAG, "WifiReceiver/onReceiver/wifiResults: " + wifiResults);

            // RecyclerView adapter 설정

            // 이미 등록된 mac address를 제거하고 보내준다.
            List<AccessPoint> apList = mRoom.accessPointDao().getAll();
            for(int i = 0; i < wifiResults.size(); i++) {
                for(int j = 0; j < apList.size(); j++) {
                    if(wifiResults.get(i).SSID.equals(apList.get(j).getSsid())) {

                    }
                }
                if(wifiResults.get(i).SSID )
            }
            wifiResultRVAdapter.addData((ArrayList<ScanResult>) wifiResults);
            wifiResultRVAdapter.notifyDataSetChanged();
        }
    }

    // 생명주기 - onDestroy()
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!wasWifiEnabled) {
            wifiManager.setWifiEnabled(false);
        }
    }
}
