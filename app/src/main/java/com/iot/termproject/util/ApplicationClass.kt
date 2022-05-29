package com.iot.termproject.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class ApplicationClass : Application() {

    companion object {
        const val TAG: String = "APP"
        const val USER_INFO: String = "USER_ID_INFO"
        lateinit var mSharedPreferences: SharedPreferences
        lateinit var USER_ID: String
    }

    @Override
    override fun onCreate() {
        super.onCreate()
        mSharedPreferences = applicationContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
    }
}
