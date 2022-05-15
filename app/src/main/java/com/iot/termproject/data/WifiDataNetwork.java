package com.iot.termproject.data;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 같은 SSID라도 access point 기기가 다를 수 있다.
 * 따라서 전환되었는지 알아볼 때 access point의 mac address를 이용한다.
 *
 * 참조: http://egloos.zum.com/geneus/v/3519790
 */
public class WifiDataNetwork implements Comparable<WifiDataNetwork>, Parcelable {
    // BSSID: access point 주소
    private String bssid;

    // SSID: 네트워크 이름
    private String ssid;

    // capabilities
    private String capabilities;

    // frequency
    private int frequency;

    // level: RSSI 값, 즉 관측된 신호의 레벨 (dBm 단위)
    // 최대: -55, 최소: -100
    private int level;

    // timestamp: 마지막으로 관측된 시간 (microseconds 단위)
    private long timestamp;

    // Constructor
    public WifiDataNetwork(ScanResult result) {
        bssid = result.BSSID;
        ssid = result.SSID;
        capabilities = result.capabilities;
        frequency = result.frequency;
        level = result.level;
        timestamp = System.currentTimeMillis();
    }

    // Constructor
    // @param in - Parcel 객체
    // 읽어와서 변수에 할당
    public WifiDataNetwork(Parcel in) {
        bssid = in.readString();
        ssid = in.readString();
        capabilities = in.readString();
        frequency = in.readInt();
        level = in.readInt();
        timestamp = in.readLong();
    }

    // Parcelable.Creator:
    // Interface that must be implemented and provided as a public CREATOR field
    // that generates instances of your Parcelable class from a Parcel.
    // android.os.Parcelable
    public static final Creator<WifiDataNetwork> CREATOR = new Creator<WifiDataNetwork>() {
        public WifiDataNetwork createFromParcel(Parcel in) {
            return new WifiDataNetwork(in);
        }

        @Override
        public WifiDataNetwork[] newArray(int i) {
            return new WifiDataNetwork[i];
        }
    };

    // Wi-Fi frequency (int) -> corresponding channel (int)
    // @param freq - frequency as given by ScanResult frequency
    public static int convertFrequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) return (freq - 2412) / 5 + 1;
        else if (freq >= 5170 && freq <= 5825) return (freq - 5170) / 5 + 34;
        else return -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Parcel에 값 입력하기
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(bssid);
        parcel.writeString(ssid);
        parcel.writeString(capabilities);
        parcel.writeInt(frequency);
        parcel.writeLong(timestamp);
    }

    @Override
    public int compareTo(WifiDataNetwork wifiDataNetwork) {
        return wifiDataNetwork.level - this.level;
    }

    @Override
    public String toString() {
        return ssid + ", address(bssid):" + bssid + ", level:" + level + ", dBm freq:" + frequency + ", MHz cap:" + capabilities;
    }

    public String getBssid() {
        return bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getLevel() {
        return level;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
