package com.iot.termproject.data.local

import androidx.room.*
import com.iot.termproject.data.entity.ReferencePoint

@Dao
interface ReferencePointDao {
    @Insert
    fun insert(referencePoint: ReferencePoint)

    @Update
    fun update(referencePoint: ReferencePoint)

    @Delete
    fun delete(referencePoint: ReferencePoint)

    @Query("SELECT * FROM room_point")
    fun getAll(): List<ReferencePoint>

    @Query("SELECT * FROM room_point WHERE name = :name")
    fun getRoomPointByName(name: String): ReferencePoint
}