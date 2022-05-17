package com.iot.termproject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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
    private static final int timeOut = 2800;

    // ViewBinding 설정
    @Override
    protected ActivitySplashBinding setViewBinding() {
        return ActivitySplashBinding.inflate(getLayoutInflater());
    }

    // ViewBinding 이후 처리할 작업들을 넣어준다.
    // onCreate() 생명주기 이후
    @Override
    protected void initAfterBinding() {
        // animation 추가
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_splash);
        binding.splashTitleTv.startAnimation(anim);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startNextActivity(MainActivity.class);
                finish();
            }
        }, timeOut);
    }
}
