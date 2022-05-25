package com.iot.termproject.data.local

import androidx.room.*
import com.iot.termproject.data.entity.AccessPoint

@Dao
interface AccessPointDao {
    @Insert
    fun insert(accessPoint: AccessPoint)

    @Update
    fun update(accessPoint: AccessPoint)

    @Delete
    fun delete(accessPoint: AccessPoint)

    @Query("SELECT * FROM access_point")
    fun getAll(): List<AccessPoint>

    @Query("SELECT * FROM access_point WHERE ssid = :ssid")
    fun getAccessPointBySsid(ssid: String): AccessPoint

    @Query("SELECT macAddress FROM access_point WHERE ssid = :ssid")
    fun getMacAddressBySsid(ssid: String): AccessPoint

    @Query("SELECT * FROM access_point WHERE macAddress = :macAddress")
    fun getAccessPointByMacAddress(macAddress: String)
}