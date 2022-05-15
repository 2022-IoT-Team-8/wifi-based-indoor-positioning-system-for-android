package com.iot.termproject.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iot.termproject.data.entity.AccessPoint

class Converter {

    // List<AccessPoint> -> Json (AccessPoint) 변환
    @TypeConverter
    fun listToJson(value: List<AccessPoint>?) = Gson().toJson(value)

    // Json (AccessPoint) -> List<AccessPoint> 변환
    @TypeConverter
    fun jsonToList(value: String?) = Gson().fromJson(value, Array<AccessPoint>::class.java).toList()
}
