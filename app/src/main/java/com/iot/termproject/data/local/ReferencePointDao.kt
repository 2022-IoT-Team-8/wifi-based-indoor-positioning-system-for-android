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

    @Query("SELECT * FROM room_point WHERE id = :id")
    fun getRoomPointById(id: Int): ReferencePoint

    @Query("DELETE FROM room_point WHERE id = :id")
    fun deleteById(id: Int)
}
