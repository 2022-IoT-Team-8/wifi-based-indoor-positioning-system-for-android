package com.iot.termproject.admin.data;

public class Floor {
    // 몇 층 (2F, 4F, 5F)
    int floor;

    // 최대 몇 호까지 있는지 (230, 434, 532)
    int maxRoomNumber;

    public Floor(int floor, int maxRoomNumber) {
        this.floor = floor;
        this.maxRoomNumber = maxRoomNumber;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getFloor() {
        return floor;
    }

    public void setMaxRoomNumber(int maxRoomNumber) {
        this.maxRoomNumber = maxRoomNumber;
    }

    public int getMaxRoomNumber() {
        return maxRoomNumber;
    }
}
