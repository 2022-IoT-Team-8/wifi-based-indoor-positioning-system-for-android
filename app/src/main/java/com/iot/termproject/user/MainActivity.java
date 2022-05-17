package com.iot.termproject.user;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.iot.termproject.admin.AccessPointActivity;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.databinding.ActivityUserMainBinding;

/**
 * 유저의 위치 정보를 받아와 특정 위치를 (E.g., 강의실 **호) 보여주는 화면
 * 현재는 사용 안 함.
 */
public class MainActivity extends BaseActivity<ActivityUserMainBinding> {
    @Override
    protected ActivityUserMainBinding setViewBinding() {
        return ActivityUserMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initAfterBinding() {
        // ActionBar 수정
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setTitle("User");

        initClickListener();
    }

    // click listener 초기화
    private void initClickListener() {
        binding.mainCheckBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO: 데이터베이스에서 데이터를 가져와서 세팅해줘야 한다.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // 위치 정보 허용
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 197);
                } else {
                    startNextActivity(LocateMeActivity.class);
                }

                binding.mainAnswerTv.setText("");
            }
        });
    }

    // Android 권한 요청 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 197 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startNextActivity(LocateMeActivity.class);
        }
    }
}
