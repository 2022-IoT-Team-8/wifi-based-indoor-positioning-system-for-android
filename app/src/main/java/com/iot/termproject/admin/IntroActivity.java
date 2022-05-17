package com.iot.termproject.admin;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;

import com.iot.termproject.R;
import com.iot.termproject.admin.adapter.IntroAdapter;
import com.iot.termproject.admin.data.Floor;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.databinding.ActivityIntroBinding;

import java.util.ArrayList;

public class IntroActivity extends BaseActivity<ActivityIntroBinding> {
    IntroAdapter adapter = null;
    ArrayList<Floor> floors = new ArrayList<Floor>();

    @Override
    protected ActivityIntroBinding setViewBinding() {
        return ActivityIntroBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initAfterBinding() {

        // ActionBar 수정
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setTitle("Admin");

        initFloorData();
        initListView();
        initClickListener();
    }

    private void initFloorData() {
        // 2F, 4F, 5F에 존재하는 가장 높은 호실 추가
        floors.add(new Floor(2, 230));
        floors.add(new Floor(4, 434));
        floors.add(new Floor(5, 532));
    }

    private void initListView() {
        // adapter setting
        ListView listView = findViewById(R.id.intro_list_view);
        adapter = new IntroAdapter(this, floors);
        listView.setAdapter(adapter);
    }

    // click listener 초기화
    private void initClickListener() {

        // 각각 floor 버튼 클릭 시 이동
        // 이동 시 몇 층인지 전달
        binding.introListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);    // MainRoomPointActivity

                switch (position) {
                    case 0:
                        // 2층
                        intent.putExtra("floor", 2);
                        break;
                    case 1:
                        // 4층
                        intent.putExtra("floor", 4);
                        break;
                    case 2:
                        // 5층
                        intent.putExtra("floor", 5);
                        break;
                    default:
                        break;
                }

                startActivity(intent);
            }
        });
    }
}
