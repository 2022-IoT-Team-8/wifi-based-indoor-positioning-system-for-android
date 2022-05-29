package com.iot.termproject;

import android.view.View;

import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.databinding.ActivityMainBinding;
import com.iot.termproject.ui.user.LocationActivity;

/**
 * 관리자 모드(admin), 사용자 모드(user)를 선택할 수 있는 화면
 *
 * @see LocationActivity 사용자 모드에서 위치를 확인할 수 있는 화면
 * @see com.iot.termproject.ui.admin.MainActivity 관리자 메인 화면
 */
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
                // 위치를 확인할 수 있는 화면으로 넘어간다.
                startNextActivity(LocationActivity.class);
            }
        });

        // admin mode (관리자 모드)
        binding.mainAdminBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startNextActivity(com.iot.termproject.ui.admin.MainActivity.class);
            }
        });
    }
}
