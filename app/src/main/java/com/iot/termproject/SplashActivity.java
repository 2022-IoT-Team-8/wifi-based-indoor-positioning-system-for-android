package com.iot.termproject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.databinding.ActivitySplashBinding;

/**
 * 초기 화면 (애니메이션 효과와 함께 타이틀이 뜬다.)
 *
 * @see com.iot.termproject.ui.admin.MainActivity 관리자 모드
 * @see com.iot.termproject.ui.user.MainActivity 사용자 모드
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<ActivitySplashBinding> {
    private static final int timeOut = 2800;

    // ViewBinding 설정
    @Override
    protected ActivitySplashBinding setViewBinding() {
        return ActivitySplashBinding.inflate(getLayoutInflater());
    }

    // onCreate() 생명주기 이후 (ViewBinding 이후)
    @Override
    protected void initAfterBinding() {
        // animation 추가
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_splash);
        binding.splashTitleTv.startAnimation(anim);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 모드를 선택할 수 있는 화면으로 넘어간다.
                startNextActivity(MainActivity.class);
                finish();
            }
        }, timeOut);
    }
}
