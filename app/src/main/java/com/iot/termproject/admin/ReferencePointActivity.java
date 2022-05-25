package com.iot.termproject.admin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;

import com.iot.termproject.ApplicationClass;
import com.iot.termproject.adapter.AccessPointRVAdapter;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.entity.AccessPoint;
import com.iot.termproject.data.entity.ReferencePoint;
import com.iot.termproject.databinding.ActivityRoomPointBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Room point를 추가하거나 수정할 수 있는 화면
 *
 * @see MainActivity 로부터 넘어온다.
 * @see AccessPointRVAdapter
 *
 * TODO: RecyclerView & 추가 설정 & 이해 더 해보기기
 */
public class ReferencePointActivity extends BaseActivity<ActivityRoomPointBinding> {
    private static final String TAG = "ACT/RP";

    private boolean isEdit = false;
    private AppDatabase mRoom;

    // RecyclerView
    private AccessPointRVAdapter accessPointRVAdapter;
    private final List<AccessPoint> accessPoints = new ArrayList<>();
    private final Map<String, List<Integer>> macToLevelsMap = new HashMap<>();
    private final Map<String, AccessPoint> macToAccessPointMap = new HashMap<>();

    // Wi-Fi
    private WifiManager wifi;
    private boolean isEnable;
    private boolean isCaliberating = false;
    private int readingCount = 0;

    private AvailableAPsReceiver receiverWifi;

    // Thread
    private final Handler handler = new Handler();

    // RoomPoint
    private ReferencePoint referencePoint;
    private String roomPointName;

    // ViewBinding 설정
    @Override
    protected ActivityRoomPointBinding setViewBinding() {
        return ActivityRoomPointBinding.inflate(getLayoutInflater());
    }

    // ViewBinding 이후 처리할 작업들을 넣어준다.
    // onCreate() 생명주기 이후
    @Override
    protected void initAfterBinding() {
        // RoomDB 데이터베이스 객체 생성
        database = AppDatabase.Companion.getInstance(this);

        // Room point 정보를 받아왔을 때 해당 정보가 이미 존재한다면 편집 모드로 들어간다.
        if (!getIntent().getStringExtra("room_point_name").equals("")) {
            // 편집 모드인 경우
            isEdit = true;
            roomPointName = getIntent().getStringExtra("room_point_name");
        }

        Log.d(TAG, "initAfterBinding/isEdit: " + isEdit);

        // RecyclerView 초기화
        initRecyclerView();

        // 편집 모드인지 아닌지에 따라 그에 맞는 처리들을 해준다.
        if (isEdit) {

            // 편집 모드일 경우
            // 데이터베이스로부터 해당 room point 객체를 불러온다.
            referencePoint = database.referencePointDao().getRoomPointByName(roomPointName);

            // room point 객체가 갖고 있는 access point들을 불러와 저장한다.
            ArrayList<AccessPoint> accessPoints = (ArrayList<AccessPoint>) referencePoint.getAccessPointList();
            Log.d(TAG, "initAfterBinding/accessPoints: " + accessPoints);

            // RecyclerView adapter 설정
            // Access point들을 RecyclerView를 이용해서 보여준다.
            if(accessPoints != null) {
                accessPointRVAdapter.addData(accessPoints);
            }
            binding.addRoomPointRecyclerView.setAdapter(accessPointRVAdapter);

            // 편집 모드에 알맞게 view setting
            binding.addRoomPointApNameEt.setText(referencePoint.getName());
            binding.addRoomApXEt.setText(String.valueOf(referencePoint.getLatitude()));
            binding.addRoomApYEt.setText(String.valueOf(referencePoint.getLongitude()));
        } else {
            // 편집 모드가 아닐 경우
            // WifiManager 및 AcailableAPsReciever 객체를 생성한다.
            wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            receiverWifi = new AvailableAPsReceiver();
            isEnable = wifi.isWifiEnabled();

            // 데이터베이스에 있는 모든 access point들을 불러온다.
            // FixMe: 이렇게 하는 게 맞는지 검토하기
            ArrayList<AccessPoint> accessPoints = (ArrayList<AccessPoint>) database.accessPointDao().getAll();
            Log.d(TAG, "initAfterBinding/accessPoints: " + accessPoints);

            for (AccessPoint accessPoint : accessPoints) {
                // key: access point의 mac address
                // value: 해당 access point 객체
                macToAccessPointMap.put(accessPoint.getMacAddress(), accessPoint);
            }

            // TODO: 비어있거나 GPS 기능이 켜져 있지 않은 경우 알림을 주는 기능을 추가적으로 고려해보기
        }

        // UI 초기화
        initUI();

        // Click listener 초기화
        initClickListener();
    }

    // 생명주기 - onResume()
    @Override
    protected void onResume() {
        if(!isEdit) {
            // 편집 모드가 아닌 경우 Wi-Fi receiver 등록
            registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            if(!isCaliberating) {
                isCaliberating = true;
                refresh();
            }
        }
        super.onResume();
    }

    // 생명주기 - onPause()
    @Override
    protected void onPause() {
        if(!isEdit) {
            unregisterReceiver(receiverWifi);
            isCaliberating = false;
        }
        super.onPause();
    }

    // Refresh
    public void refresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifi.startScan();

                // 3초마다 10개씩
                if(readingCount < 10) refresh();
                else caliberationCompleted();
            }
        }, 3000);
    }

    // Caliberation 완료된 경우
    private void caliberationCompleted() {
        // 완료되었으므로 상태 변수를 false로 바꿔준다.
        isCaliberating = false;

        Map<String, List<Integer>> macToLevelsMap = this.macToLevelsMap;

        for(Map.Entry<String, List<Integer>> entry : macToLevelsMap.entrySet()) {
            // 각각에 해당하는 level들을 불러와서 평균값을 계산해준다.
            List<Integer> levels = entry.getValue();
            Double mean = calcualteMean(levels);

            // FixMe: 데이터베이스 관련해서 맞는기 검토해보기
            // 해당 ssid를 가진 access point 객체를 데이터베이스를 통해 받아온 뒤,
            // MeanRSS 값을 추가해준다.
            String ssid = Objects.requireNonNull(macToAccessPointMap.get(entry.getKey())).getSsid();
            AccessPoint updatedAccessPoint = database.accessPointDao().getAccessPointBySsid(ssid);
            updatedAccessPoint.setMeanRss(mean);
            accessPoints.add(updatedAccessPoint);
        }

        // 데이터셋 추가
        accessPointRVAdapter.addData((ArrayList<AccessPoint>) accessPoints);

        // Caliberation 완료되었으므로 그에 맞는 view setting
        binding.addRoomPointApSaveBtn.setEnabled(true);
        binding.addRoomPointApSaveBtn.setText("Save");
    }

    // 평균값 (meanRSS) 계산
    private Double calcualteMean(List<Integer> accessPoints) {
        if(accessPoints.isEmpty()) return 0.0d;

        Integer sum = 0;
        for(Integer integer : accessPoints) sum += integer;

        return Double.valueOf(sum) / (double) accessPoints.size();
    }

    // UI 초기화
    private void initUI() {
        if(!isEdit) {
            // 편집 모드가 아닌 경우
            binding.addRoomPointApSaveBtn.setEnabled(false);
            binding.addRoomPointApSaveBtn.setText("Caliberating...");
        } else {
            // 편집 모드인 경우
            binding.addRoomPointApSaveBtn.setEnabled(true);
            binding.addRoomPointApSaveBtn.setText("Save");
        }
    }

    // RecyclerView 초기화
    private void initRecyclerView() {
        accessPointRVAdapter = new AccessPointRVAdapter();
        binding.addRoomPointRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.addRoomPointRecyclerView.setAdapter(accessPointRVAdapter);
    }

    // Click listener 초기화
    private void initClickListener() {

        // 'Save' button 클릭 시
        binding.addRoomPointApSaveBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String name = binding.addRoomPointApNameEt.getText().toString();
                String x = binding.addRoomApXEt.getText().toString();
                String y = binding.addRoomApYEt.getText().toString();

                if (!isEdit) {
                    // 편집 모드가 아닐 경우
                    // 즉, 새로 생성하는 경우

                    // x
                    double doubleX;
                    if (TextUtils.isEmpty(x)) doubleX = 0.0d;
                    else doubleX = Double.parseDouble(x);

                    // y
                    double doubleY;
                    if (TextUtils.isEmpty(y)) doubleY = 0.0d;
                    else doubleY = Double.parseDouble(y);

                    String locationId = doubleX + " " + doubleY;

                    // Insert
                    // 데이터베이스에 삽입해서 MainActivity에서 보여지도록 해야 한다.
                    // FixMe: floor & GPS 추가해야 한다.
                    database.referencePointDao().insert(new ReferencePoint(name, 2, doubleX, doubleY, accessPoints));
                    Log.d(TAG, "initClickListener/onClick/accessPoints: " + accessPoints);
                } else {
                    // 편집 모드인 경우 수정 및 업데이트만 해주면 된다.

                    // x
                    double doubleX;
                    if (TextUtils.isEmpty(x)) doubleX = 0.0d;
                    else doubleX = Double.parseDouble(x);

                    // y
                    double doubleY;
                    if (TextUtils.isEmpty(y)) doubleY = 0.0d;
                    else doubleY = Double.parseDouble(y);

                    String locationId = doubleX + " " + doubleY;

                    // 데이터베이스에서 해당 Room point를 불러와서 업데이트 해준다.
                    ReferencePoint referencePoint = database.referencePointDao().getRoomPointByName(name);
                    referencePoint.setName(name);
                    referencePoint.setLatitude(doubleX);
                    referencePoint.setLongitude(doubleY);

                    // 업데이트 해주는 부분
                    // 데이터베이스에 업데이트함으로써 MainActivity에서 보여지도록 해야 한다.
                    database.referencePointDao().update(referencePoint);
                }
                finish();
            }
        });
    }

    // Available한 access point들을 받아오기 위함이다.
    class AvailableAPsReceiver extends BroadcastReceiver {

        // onReceiver를 통해서 받는다.
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> scanResultList = wifi.getScanResults();
            ++readingCount;

            for (Map.Entry<String, AccessPoint> entry : macToAccessPointMap.entrySet()) {
                String macAddress = entry.getKey();

                for (ScanResult scanResult : scanResultList) {
                    if (entry.getKey().equals(scanResult.BSSID)) {
                        checkAndAddAccessPointRSS(macAddress, scanResult.level);
                        macAddress = null;
                        break;
                    }
                }

                if (macAddress != null)
                    checkAndAddAccessPointRSS(macAddress, ApplicationClass.NaN.intValue());
            }
        }
    }

    private void checkAndAddAccessPointRSS(String macAddress, Integer level) {
        if (macToLevelsMap.containsKey(macAddress)) {
            List<Integer> integerList = macToLevelsMap.get(macAddress);
            integerList.add(level);
        } else {
            List<Integer> integerList = new ArrayList<>();
            integerList.add(level);
            macToLevelsMap.put(macAddress, integerList);
        }
    }

    // 생명주기 - onDestroy()
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isEnable && !isEdit) wifi.setWifiEnabled(false);
    }
}
