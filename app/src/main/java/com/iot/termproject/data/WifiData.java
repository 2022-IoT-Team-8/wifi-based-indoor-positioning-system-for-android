package com.iot.termproject.data;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wi-Fi data network들을 관리한다.
 * WifiManager로부터 받아온 값들을 저장하는 용도
 * <p>
 * 추가 설명:
 * 안드로이드는 프로세스 간 데이터를 전달할 때 바인더를 통해 Parcel이라는 객체로 전달한다.
 * 이때, Parcel은 추상화된 객체로 데이터와 객체를 갖고있는 container이다.
 * 전달하려는 객체를 Parcel에 저장하고 다른 프로세스로 전달하면 된다.
 * Parcelable은 interface이며, Parcel에 객체를 write/read하도록 만들어준다.
 * 내가 정의한 클래스의 객체를 다른 activity에 전달하려면 Parcelable을 implements하여 구현해주면 된다.
 */
public class WifiData implements Parcelable {
    private List<WifiDataNetwork> mNetworks;

    // Constructor
    public WifiData() {
        mNetworks = new ArrayList<>();
    }

    // Constructor
    // @param Parcel 객체
    public WifiData(Parcel in) {
        // 읽기
        in.readTypedList(mNetworks, WifiDataNetwork.CREATOR);
    }

    // Parcelable.Creator:
    // Interface that must be implemented and provided as a public CREATOR field
    // that generates instances of your Parcelable class from a Parcel.
    public static final Creator<WifiData> CREATOR = new Creator<WifiData>() {
        public WifiData createFromParcel(Parcel in) {
            return new WifiData(in);
        }

        public WifiData[] newArray(int size) {
            return new WifiData[size];
        }
    };

    // 관리자가 수행한 마지막 와이파이 검색을 저장하고,
    // 탐지된 각 네트워크에 대한 개체를 만든다.
    public void addNetworkList(List<ScanResult> results) {
        mNetworks.clear();

        for (ScanResult result : results) {
            mNetworks.add(new WifiDataNetwork(result));
        }

        Collections.sort(mNetworks);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(mNetworks);
    }

    @NonNull
    @Override
    public String toString() {
        if (mNetworks == null || mNetworks.size() == 0) return "Empty data";
        else return mNetworks.size() + " networks data";
    }

    // return the list of scanned networks
    public List<WifiDataNetwork> getmNetworks() {
        return mNetworks;
    }
}
