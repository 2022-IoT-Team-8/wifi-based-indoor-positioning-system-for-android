package com.iot.termproject.ui.admin;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iot.termproject.adapter.ReferencePointsRVAdapter;
import com.iot.termproject.base.BaseActivity;
import com.iot.termproject.data.AppDatabase;
import com.iot.termproject.data.entity.AccessPoint;
import com.iot.termproject.data.entity.ReferencePoint;
import com.iot.termproject.databinding.ActivityAdminMainBinding;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Administrator (관리자) 모드 메인 화면
 *
 * @see ReferencePointsRVAdapter reference point들을 보여주는 RecyclerView adapter
 */
public class MainActivity extends BaseActivity<ActivityAdminMainBinding> {
    private static final String TAG = "ACT/MAIN";

    // Room
    AppDatabase mRoom;
    ArrayList<AccessPoint> accessPoints = new ArrayList<>();
    ArrayList<ReferencePoint> referencePoints = new ArrayList<>();

    // firebase
    private FirebaseDatabase mDatabase;
    private DatabaseReference mAPReference;
    private DatabaseReference mRPReference;

    // ViewBinding 설정
    @Override
    protected ActivityAdminMainBinding setViewBinding() {
        return ActivityAdminMainBinding.inflate(getLayoutInflater());
    }

    // onCreate() 생명주기 이후 (ViewBinding 이후)
    @Override
    protected void initAfterBinding() {
        mDatabase = FirebaseDatabase.getInstance("https://iot-term-project-team8-default-rtdb.firebaseio.com/");
        mAPReference = mDatabase.getReference("Access_point");
        mRPReference = mDatabase.getReference("reference_points");
        mRoom = AppDatabase.Companion.getInstance(this);

        initData();

        accessPoints = (ArrayList<AccessPoint>) mRoom.accessPointDao().getAll();
        referencePoints = (ArrayList<ReferencePoint>) mRoom.referencePointDao().getAll();

        initRecyclerView();
        initClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        accessPoints = (ArrayList<AccessPoint>) mRoom.accessPointDao().getAll();
        referencePoints = (ArrayList<ReferencePoint>) mRoom.referencePointDao().getAll();

        initRecyclerView();
    }

    // firebase data reference 초기화
    private void initData() {
        Log.d(TAG, "initData");
        Log.d(TAG, mDatabase.toString());
        Log.d(TAG, mAPReference.toString());

        // 파이어베이스에 저장된 access point들을 받아온다.
        mAPReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange");
                accessPoints.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String macAddress = dataSnapshot.getValue(String.class);

                    assert macAddress != null;
                    AccessPoint ap = new AccessPoint(macAddress, 0.0, 0.0);
                    accessPoints.add(ap);
                    mRoom.accessPointDao().insert(ap);  // 로컬에 추가

                    referencePoints = (ArrayList<ReferencePoint>) mRoom.referencePointDao().getAll();
                }

                for (int i = 0; i < accessPoints.size(); i++) {
                    mRoom.accessPointDao().insert(accessPoints.get(i));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // RecyclerView 초기화
    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView");

        // RecyclerView
        ReferencePointsRVAdapter rpRVAdapter = new ReferencePointsRVAdapter(this, new ReferencePointsRVAdapter.MyItemClickListener() {

            @Override
            public void onItemLongClick(int position) {
                mRoom.referencePointDao().deleteById(referencePoints.get(position).getId());
            }

            @Override
            public void onItemClick(@NonNull View view, int position) {
                ReferencePoint referencePoint = referencePoints.get(position);
                Intent intent = new Intent(getApplicationContext(), ReferencePointActivity.class);
                intent.putExtra("reference_point_id", String.valueOf(referencePoint.getId()));
                startActivity(intent);
            }
        });
        rpRVAdapter.addData(referencePoints);
        binding.mainRpRecyclerView.setAdapter(rpRVAdapter);
    }

    // click listener 초기화
    private void initClickListener() {

        // 'Add Reference Point' 버튼 클릭 시 'ReferencePointActivity'로 전환
        binding.mainAddRpBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // 위치 정보 허용
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 198);
                } else {
                    Intent intent = new Intent(getApplicationContext(), ReferencePointActivity.class);
                    intent.putExtra("reference_point_id", "");
                    startActivity(intent);
                }
            }
        });

        // 'Get Data' 버튼 클릭 시 파이어베이스로부터 데이터 받아오기
        binding.mainGetDataBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                initData();
            }
        });

        // 'Set Data' 버튼 클릭 시 파이어베이스에 데이터 전송
        binding.mainSetDataBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "set data");

                for (int i = 0; i < referencePoints.size(); i++) {
                    String id = String.valueOf(referencePoints.get(i).getId());

                    mRPReference.child(id).child("reference_point").setValue(referencePoints.get(i).getName());
                    mRPReference.child(id).child("latitude").setValue(referencePoints.get(i).getLatitude());
                    mRPReference.child(id).child("longitude").setValue(referencePoints.get(i).getLongitude());
                    mRPReference.child(id).child("floor").setValue(referencePoints.get(i).getFloor());

                    if (referencePoints.get(i).getAccessPoints() != null && Objects.requireNonNull(referencePoints.get(i).getAccessPoints()).size() > 0) {
                        ArrayList<AccessPoint> aps = (ArrayList<AccessPoint>) referencePoints.get(i).getAccessPoints();

                        if (aps != null && aps.size() > 0) {
                            Log.d(TAG, "access points: " + aps);

                            for (int j = 0; j < aps.size(); j++) {
                                mRPReference.child(id).child(aps.get(j).getMacAddress()).setValue(aps.get(i).getMeanRss());
                            }
                        }
                    }
                }
            }
        });
    }

    // Android 권한 요청 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 198 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(getApplicationContext(), ReferencePointActivity.class);
            intent.putExtra("reference_point_id", "");
            startActivity(intent);
        }
    }
}
