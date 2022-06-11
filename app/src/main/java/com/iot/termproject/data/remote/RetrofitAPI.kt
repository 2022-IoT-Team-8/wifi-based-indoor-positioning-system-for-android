package com.iot.termproject.data.remote

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface RetrofitAPI {

    // 스캔 결과 보내주기
    @POST("/predict2")
    fun sendScanResult(
        @Body scanResult: ScanResult
    ): Call<JsonObject>

}
