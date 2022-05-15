package com.iot.termproject.data

import android.content.Context
import androidx.room.*
import com.iot.termproject.data.entity.AccessPoint
import com.iot.termproject.data.entity.RoomPoint
import com.iot.termproject.data.local.AccessPointDao
import com.iot.termproject.data.local.RoomPointDao

@Database(entities = [AccessPoint::class, RoomPoint::class], version = 1)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accessPointDao(): AccessPointDao
    abstract fun roomPointDao(): RoomPointDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase? {
            if(instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "database"
                    ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
                }
            }
            return instance
        }
    }
}