package com.iot.termproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "room_point")
data class ReferencePoint(
    @SerializedName("name") var name: Int,
    @SerializedName("floor") var floor: Int,
    @SerializedName("latitude") var latitude: Double,       // 위도
    @SerializedName("longitude") var longitude: Double,     // 경도
    @SerializedName("access_point_list") var accessPoints: List<AccessPoint>?
) {
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
