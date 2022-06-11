package com.iot.termproject.data.remote

import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.iot.termproject.ApplicationClass.retrofit
import com.iot.termproject.ScanResultView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanResultService {
    companion object { private const val TAG = "SERVICE/SCAN-RESULT" }

    fun scanResult(scanResultView: ScanResultView, scanResult: ScanResult) {
        val retrofitAPIService = retrofit.create(RetrofitAPI::class.java);

        // interface 함수 작성
        retrofitAPIService.sendScanResult(scanResult).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "onResponse/result: ${response.body()}")

                // FixMe: 서버 측에서 어떻게 보낼 건지 정한 후 작성해야 함.
                val mGson = Gson()
                val result = mGson.fromJson(response.body(), Result::class.java)
                scanResultView.onScanResultSuccess(result)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "onFailure")
                scanResultView.onScanResultFailure()
            }
        })
    }
}
