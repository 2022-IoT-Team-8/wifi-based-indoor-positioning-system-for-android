package com.iot.termproject.admin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.iot.termproject.ApplicationClass;
import com.iot.termproject.GpsTracker;
import com.iot.termproject.adapter.AccessPointRVAdapter;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.entity.AccessPoint;
import com.iot.termproject.data.entity.RoomPoint;
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
public class RoomPointActivity extends BaseActivity<ActivityRoomPointBinding> {
    private static final String TAG = "ACT/RP";

    private boolean isEdit = false;
    private AppDatabase database;

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
    private RoomPoint roomPoint;
    private String roomPointName;

    // ViewBinding 설정
    @Override
    protected ActivityRoomPointBinding setViewBinding() {
        return ActivityRoomPointBinding.inflate(getLayoutInflater());
    }

    //위도 경도 받아 오는 것 관련
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

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
            roomPoint = database.roomPointDao().getRoomPointByName(roomPointName);

            // room point 객체가 갖고 있는 access point들을 불러와 저장한다.
            ArrayList<AccessPoint> accessPoints = (ArrayList<AccessPoint>) roomPoint.getAccessPointList();
            Log.d(TAG, "initAfterBinding/accessPoints: " + accessPoints);

            // RecyclerView adapter 설정
            // Access point들을 RecyclerView를 이용해서 보여준다.
            if(accessPoints != null) {
                accessPointRVAdapter.addData(accessPoints);
            }
            binding.addRoomPointRecyclerView.setAdapter(accessPointRVAdapter);

            // 편집 모드에 알맞게 view setting
            binding.addRoomPointApNameEt.setText(roomPoint.getName());
            binding.addRoomApXEt.setText(String.valueOf(roomPoint.getX()));
            binding.addRoomApYEt.setText(String.valueOf(roomPoint.getY()));
        } else {
            //Todo: 층을 다루는 Spinner 셋팅
            final String[] floor = {"floor", "2F", "4F", "5F"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, floor);
            binding.addRoomFloor.setAdapter(adapter);

            //Todo: 층에 따라 호실을 다루는 Spinner 셋팅

            // 편집 모드가 아닐 경우
            setMyPosition();

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
                macToAccessPointMap.put(accessPoint.getMac_address(), accessPoint);
            }

            // TODO: 비어있거나 GPS 기능이 켜져 있지 않은 경우 알림을 주는 기능을 추가적으로 고려해보기
        }

        // UI 초기화
        initUI();

        // Click listener 초기화
        initClickListener();
    }

    // 위도와 경도를 EditText에 설정하기
    protected void setMyPosition(){
        if (checkLocationServicesStatus()) {
            checkRunTimePermission();
        } else {
            showDialogForLocationServiceSetting();
        }

        gpsTracker = new GpsTracker(RoomPointActivity.this);

        String latitude = Double.toString(gpsTracker.getLatitude());//위도 받아오기
        String longitude = Double.toString(gpsTracker.getLongitude());//경도 받아오기

        //EditText에 위도 경도 설정하기
        binding.addRoomApXEt.setText(latitude);// X에 위도 셋팅하기
        binding.addRoomApYEt.setText(longitude);// Y에 경도 셋팅하기

    }


    //ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료. 2 가지 경우가 있다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(RoomPointActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                } else {

                    Toast.makeText(RoomPointActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission() {
        // 런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(RoomPointActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(RoomPointActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(RoomPointActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(RoomPointActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RoomPointActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RoomPointActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(RoomPointActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    // 위도 경도 받아오는 코드 끄읏!

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
            AccessPoint updatedAccessPoint = database.accessPointDao().getAccessPointById(ssid);
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

                    //Todo: Spinner에서 몇 층인지 받아와서 저장

                    // Insert
                    // 데이터베이스에 삽입해서 MainActivity에서 보여지도록 해야 한다.
                    database.roomPointDao().insert(new RoomPoint(name, null, doubleX, doubleY, locationId, accessPoints));
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
                    RoomPoint roomPoint = database.roomPointDao().getRoomPointByName(name);
                    roomPoint.setName(name);
                    roomPoint.setX(doubleX);
                    roomPoint.setY(doubleY);
                    roomPoint.setLocationId(locationId);

                    // 업데이트 해주는 부분
                    // 데이터베이스에 업데이트함으로써 MainActivity에서 보여지도록 해야 한다.
                    database.roomPointDao().update(roomPoint);
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
