package com.iot.termproject.data.local

import androidx.room.*
import com.iot.termproject.data.entity.RoomPoint

@Dao
interface RoomPointDao {
    @Insert
    fun insert(roomPoint: RoomPoint)

    @Update
    fun update(roomPoint: RoomPoint)

    @Delete
    fun delete(roomPoint: RoomPoint)

    @Query("SELECT * FROM room_point")
    fun getAll(): List<RoomPoint>

    @Query("SELECT * FROM room_point WHERE name = :name")
    fun getRoomPointByName(name: String): RoomPoint
}