package com.iot.termproject.util

fun saveUserId(userId: String) {
    val editor = ApplicationClass.mSharedPreferences.edit()
    editor.putString(ApplicationClass.USER_INFO, userId)
    editor.apply()
}

fun getUserId() : String? = ApplicationClass.mSharedPreferences.getString(ApplicationClass.USER_INFO, "")
