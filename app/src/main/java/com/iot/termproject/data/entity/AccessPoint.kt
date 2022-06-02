package com.iot.termproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "access_point")
data class AccessPoint(
    @SerializedName("mac_address") var macAddress: String,
    @SerializedName("rssi") var rssi: Double?,
    @SerializedName("mean_rss") var meanRss: Double?
) : Serializable {
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
