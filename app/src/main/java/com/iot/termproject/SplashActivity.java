package com.iot.termproject;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Toast;

import com.iot.termproject.admin.MainActivity;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.databinding.ActivitySplashBinding;

/**
 * 초기 화면
 * 관리자 모드(admin), 사용자 모드(user)를 선택할 수 있다.
 *
 * @see com.iot.termproject.admin.MainActivity 관리자 모드
 * @see com.iot.termproject.user.MainActivity 사용자 모드
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<ActivitySplashBinding> {

    // ViewBinding 설정
    @Override
    protected ActivitySplashBinding setViewBinding() {
        return ActivitySplashBinding.inflate(getLayoutInflater());
    }

    // ViewBinding 이후 처리할 작업들을 넣어준다.
    // onCreate() 생명주기 이후
    @Override
    protected void initAfterBinding() {

        // 관리자 모드 버튼 클릭 시 해당 화면으로 전환된다.
        binding.splashAdministratorBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "관리자 모드", Toast.LENGTH_SHORT).show();
                startNextActivity(com.iot.termproject.admin.MainActivity.class);
            }
        });

        // 사용자 모드 버튼 클릭 시 해당 화면으로 전환된다.
        binding.splashUserBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "사용자 모드", Toast.LENGTH_SHORT).show();
                startNextActivity(com.iot.termproject.user.MainActivity.class);
            }
        });
    }
}
