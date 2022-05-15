package com.iot.termproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Database Table
 * Access Point
 */
@Entity(tableName = "access_point")
data class AccessPoint(
    @SerializedName("ssid") var ssid: String,   // SSID or Name (at MainActivity)
    @SerializedName("description") var description: String?,
    @SerializedName("bssid") var bssid: String,
    @SerializedName("mac_address") var mac_address: String, // (at MainActivity)
    @SerializedName("x") var x: Double, // (at MainActivity)
    @SerializedName("y") var y: Double, // (at MainActivity)

    // for RP (-50 to -100)
    // High quality: 90% ~= -55db
    // Medium quality: 50% ~= -75db
    // Low qaulity: 30% ~= -85dp
    // Unusable quality: 8% ~= -96db
    @SerializedName("mean_rss") var meanRss: Double?
): Serializable {
    @SerializedName("id") @PrimaryKey(autoGenerate = true) var id: Int = 0
}
