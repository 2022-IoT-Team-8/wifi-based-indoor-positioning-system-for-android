package com.iot.termproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Database Table
 * Room Point
 */
@Entity(tableName = "room_point")
data class RoomPoint(
    @SerializedName("name") var name: String,
    @SerializedName("description") var description: String?,
    @SerializedName("x") var x: Double,
    @SerializedName("y") var y: Double,
    @SerializedName("location_id") var locationId: String,
    // If some AP is not accessible at this RP then put the least RSS value i.e. NaN in Algorithms.java
    @SerializedName("access_point_list") var accessPointList: List<AccessPoint>?
) {
    @SerializedName("local_id") @PrimaryKey(autoGenerate = true) var id: Int = 0
}
