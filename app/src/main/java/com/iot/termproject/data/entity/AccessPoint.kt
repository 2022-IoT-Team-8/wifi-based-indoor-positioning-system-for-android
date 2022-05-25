package com.iot.termproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "access_point")
data class AccessPoint(
    @SerializedName("ssid") var ssid: String,               // SSID or Name
    @SerializedName("mac_address") var macAddress: String,
    @SerializedName("mean_rss") var meanRss: Double?
) : Serializable {
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
