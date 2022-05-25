package com.iot.termproject.admin;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iot.termproject.adapter.MainAccessPointListRVAdapter;
import com.iot.termproject.adapter.MainReferencePointListRVAdapter;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.entity.AccessPoint;
import com.iot.termproject.data.entity.ReferencePoint;
import com.iot.termproject.databinding.ActivityAdminMainBinding;
import com.iot.termproject.user.LocateMeActivity;

import java.util.ArrayList;

/**
 * Administrator (관리자) 모드
 * Main 화면
 *
 * @see MainAccessPointListRVAdapter Access point들을 보여주는 RecyclerView adapter
 * @see MainReferencePointListRVAdapter Room point들을 보여주는 RecyclerView adapter
 */
public class MainActivity extends BaseActivity<ActivityAdminMainBinding> {
    private static final String TAG = "ACT/ADMIN-MAIN";

    // Room
    AppDatabase mRoom;
    ArrayList<AccessPoint> accessPoints = new ArrayList<>();
    ArrayList<ReferencePoint> referencePoints = new ArrayList<>();

    // RecyclerView
    private MainAccessPointListRVAdapter apRVAdapter;
    private MainReferencePointListRVAdapter rpRVAdapter;

    // Firebase
    private FirebaseDatabase mDatabase;
    private DatabaseReference mAPReference;
    private DatabaseReference mRPReference;

    // ViewBinding 설정
    @Override
    protected ActivityAdminMainBinding setViewBinding() {
        return ActivityAdminMainBinding.inflate(getLayoutInflater());
    }

    // ViewBinding 이후 처리할 작업들을 넣어준다.
    // onCreate() 생명주기 이후
    @Override
    protected void initAfterBinding() {
        mRoom = AppDatabase.Companion.getInstance(this);
        mDatabase = FirebaseDatabase.getInstance();
        mAPReference = mDatabase.getReference("aps");
        mRPReference = mDatabase.getReference("rps");

        accessPoints = (ArrayList<AccessPoint>) mRoom.accessPointDao().getAll();
        referencePoints = (ArrayList<ReferencePoint>) mRoom.referencePointDao().getAll();

        initRecyclerView();
        initClickListener();
    }

    // 생명주기 - onResume()
    @Override
    protected void onResume() {
        super.onResume();

        // onResume에서 한 번 더 데이터베이스에서 받아오는 작업을 수행해준다.

        accessPoints = (ArrayList<AccessPoint>) mRoom.accessPointDao().getAll();
        referencePoints = (ArrayList<ReferencePoint>) mRoom.referencePointDao().getAll();

        apRVAdapter.addData(accessPoints);
        rpRVAdapter.addData(referencePoints);
    }

    // RecyclerView 초기화
    private void initRecyclerView() {
        // TODO: Access point RecyclerView & click listener (데이터 삽입)
        apRVAdapter = new MainAccessPointListRVAdapter(this, new MainAccessPointListRVAdapter.MyItemClickListener() {

            @Override
            public void onItemLongClick() {
                // TODO: 삭제
            }

            @Override
            public void onItemClick(View view, int position) {
                // FixMe: 잘 돌아가는지 확인해보기
                AccessPoint accessPoint = accessPoints.get(position);
                Intent intent = new Intent(getApplicationContext(), AccessPointActivity.class);
                intent.putExtra("access_point_ssid", accessPoint.getSsid());
                startActivity(intent);
            }
        });
        apRVAdapter.addData(accessPoints);
        binding.mainApRecyclerView.setAdapter(apRVAdapter);

        // TODO: Room point (강의실) RecyclerView & click listener (데이터 삽입)
        rpRVAdapter = new MainReferencePointListRVAdapter(this, new MainReferencePointListRVAdapter.MyItemClickListener() {

            @Override
            public void onItemLongClick() {
                // TODO: 삭제
            }

            @Override
            public void onItemClick(View view, int position) {
                // FixMe: 잘 돌아가는지 확인해보기
                ReferencePoint referencePoint = referencePoints.get(position);
                Intent intent = new Intent(getApplicationContext(), ReferencePointActivity.class);
                intent.putExtra("room_point_name", referencePoint.getName());
                startActivity(intent);
            }
        });
        rpRVAdapter.addData(referencePoints);
        binding.mainRpRecyclerView.setAdapter(rpRVAdapter);
    }

    // click listener 초기화
    private void initClickListener() {

        // 'Add Access Point' 버튼 클릭 시 'AccessPointActivity'로 전환
        binding.mainAddApBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AccessPointActivity.class);
                intent.putExtra("access_point_ssid", "");
                startActivity(intent);
            }
        });

        // 'Add Room Point' 버튼 클릭 시 'RoomPointActivity'로 전환
        binding.mainAddRpBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // 위치 정보 허용
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 198);
                } else {
                    Intent intent = new Intent(getApplicationContext(), ReferencePointActivity.class);
                    intent.putExtra("room_point_name", "");
                    startActivity(intent);
                }
            }
        });

        // 'Set Data' 버튼 클릭 시 파이어베이스에 데이터 전송
        binding.mainSetDataBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
            }
        });
    }

    // Android 권한 요청 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 198 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(getApplicationContext(), AccessPointActivity.class);
            intent.putExtra("access_point_ssid", "");
            startActivity(intent);
        } else if (requestCode == 197 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startNextActivity(LocateMeActivity.class);
        }
    }
}
