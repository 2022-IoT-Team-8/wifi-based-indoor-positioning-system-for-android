package com.iot.termproject.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.entity.AccessPoint;
import com.iot.termproject.databinding.ActivityAccessPointBinding;

/**
 * Access point를 추가하거나 수정할 수 있는 화면
 *
 * @see MainActivity 로부터 넘어온다.
 * @see SearchAccessPointActivity 'Select via Scan' 버튼 클릭 시 넘어간다.
 */
public class AccessPointActivity extends BaseActivity<ActivityAccessPointBinding> {
    private static final String TAG = "ACT/AP";

    private String accessPointSsid;
    private boolean isEdit = false;
    private AppDatabase database;
    private AccessPoint accessPoint;

    // ViewBinding 설정
    @Override
    protected ActivityAccessPointBinding setViewBinding() {
        return ActivityAccessPointBinding.inflate(getLayoutInflater());
    }

    // ViewBinding 이후 처리할 작업들을 넣어준다.
    // onCreate() 생명주기 이후
    @SuppressLint("SetTextI18n")
    @Override
    protected void initAfterBinding() {
        database = AppDatabase.Companion.getInstance(this);

        // 기존의 access point 정보가 있다면 받아와 편집 모드인지를 알려준다.
        accessPointSsid = getIntent().getStringExtra("access_point_ssid");
        if(accessPointSsid.equals("")) {
            // 만약 받아온 정보가 없을 경우 편집 모드가 아닌 새로 생성하는 것임을 알려준다.
            isEdit = false;
        }
        else {
            // 만약 받아온 정보가 있을 경우 편집 모드임을 알려준다.
            isEdit = true;
            binding.accessApCreateBtn.setText("Save");
        }
        Log.d(TAG, "initAfterBinding/isEdit: " + isEdit);

        // 편집 모드일 경우 그에 맞는 처리를 해준다.
        if(isEdit) {
            initEditMode();
        }

        initClickListener();
    }

    // 편집 모드인 경우 데이터베이스에서 해당 값을 가져와서 그대로 넣어준다.
    private void initEditMode() {
        // 데이터베이스에서 해당 access point ssid를 이용해 객체를 가져온다.
        accessPoint = database.accessPointDao().getAccessPointById(accessPointSsid);

        // view setting
        binding.accessPointApSsidEt.setText(accessPoint.getSsid());
        binding.accessPointApDescEt.setText(accessPoint.getDescription());
        binding.accessApXEt.setText(String.valueOf(accessPoint.getX()));
        binding.accessApYEt.setText(String.valueOf(accessPoint.getY()));
        binding.accessApMacEt.setText(accessPoint.getMac_address());
    }

    // click listener 초기화
    private void initClickListener() {

        // 'Select via Scan' 버튼 클릭 시
        binding.accessPointApScanBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 199);
                } else {
                    Intent intent = new Intent(getApplicationContext(), SearchAccessPointActivity.class);
                    startActivityForResult(intent, 1212);
                }
            }
        });

        // 'Create' 버튼 클릭 시
        binding.accessApCreateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 사용자로부터 입력받은 값들을 저장한 후,
                final String ssid = binding.accessPointApSsidEt.getText().toString().trim();
                final String description = binding.accessPointApDescEt.getText().toString().trim();
                final String x = binding.accessApXEt.getText().toString().trim();
                final String y = binding.accessApYEt.getText().toString().trim();
                final String macAddress = binding.accessApMacEt.getText().toString().trim();

                // 데이터베이스에 저장한다.
                if(isEdit) {
                    // 편집 모드인 경우
                    accessPoint.setSsid(ssid);
                    accessPoint.setDescription(description);
                    accessPoint.setX(Double.parseDouble(x));
                    accessPoint.setY(Double.parseDouble(y));
                    accessPoint.setMac_address(macAddress);

                    // 데이터베이스에 반영
                    database.accessPointDao().update(accessPoint);
                } else {
                    // 새로 생성하는 경우
                    AccessPoint newAccessPoint = new AccessPoint(ssid, description, macAddress, macAddress, Double.parseDouble(x), Double.parseDouble(y), null);

                    // 데이터베이스에 반영
                    database.accessPointDao().insert(newAccessPoint);
                }
                finish();
            }
        });
    }

    // Android 권한 요청 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 199 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 요청한 권한이 허용되면 'SearchAccessPointActivity'로 넘어간다.
            Intent intent = new Intent(this, SearchAccessPointActivity.class);
            startActivityForResult(intent, 1212);
        }
    }

    // SearchAccessPointActivity로부터 받아온 값을 반영해준다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1212 && resultCode == RESULT_OK) {
            AccessPoint accessPoint = (AccessPoint) data.getSerializableExtra("access_point");
//            AccessPoint accessPoint = database.accessPointDao().getAccessPointById(accessPointId);
            Log.d(TAG, "onActivityResult/accessPoint: " + accessPoint);

            // view setting
            binding.accessPointApSsidEt.setText(accessPoint.getSsid());
            binding.accessPointApDescEt.setText(accessPoint.getDescription());
            binding.accessApXEt.setText(String.valueOf(accessPoint.getX()));
            binding.accessApYEt.setText(String.valueOf(accessPoint.getY()));
            binding.accessApMacEt.setText(accessPoint.getMac_address());
        }
    }
}
