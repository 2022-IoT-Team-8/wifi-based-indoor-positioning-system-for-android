package com.iot.termproject.user;

import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.databinding.ActivityUserMainBinding;

/**
 * 유저의 위치 정보를 받아와 특정 위치를 (E.g., 강의실 **호) 보여주는 화면
 */
public class MainActivity extends BaseActivity<ActivityUserMainBinding> {
    @Override
    protected ActivityUserMainBinding setViewBinding() {
        return ActivityUserMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initAfterBinding() {
    }
}
