package com.iot.termproject;

import static com.iot.termproject.ApplicationClass.BASE_URL;
import static com.iot.termproject.ApplicationClass.retrofit;

import android.view.View;

import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.databinding.ActivityMainBinding;
import com.iot.termproject.ui.user.LocationActivity;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
        // clinet definition
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30000, TimeUnit.MILLISECONDS)
                .connectTimeout(30000, TimeUnit.MILLISECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        // retrofit 초기화
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

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
