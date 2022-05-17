package com.iot.termproject;

import android.view.View;

import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    @Override
    protected ActivityMainBinding setViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initAfterBinding() {
        initClickListener();
    }

    // click listener 초기화
    private void initClickListener() {
        // user mode (사용자 모드)
        binding.mainUserBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startNextActivity(com.iot.termproject.user.LocationActivity.class);
            }
        });

        // admin mode (관리자 모드)
        binding.mainAdminBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startNextActivity(com.iot.termproject.admin.MainActivity.class);
            }
        });
    }
}
