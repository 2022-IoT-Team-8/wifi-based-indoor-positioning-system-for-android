package com.iot.termproject.data.remote

import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.iot.termproject.ApplicationClass.retrofit
import com.iot.termproject.ScanResultView
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanResultService {
    companion object { private const val TAG = "SERVICE/SCAN-RESULT" }

    // parameter로 scanResult가 아니라 jsonObject로 변경하였습니다.
    fun scanResult(scanResultView: ScanResultView, jsonObject: JSONObject) {
        Log.d(TAG, "retrofit: $retrofit")
        val retrofitAPIService = retrofit.create(RetrofitAPI::class.java);

        // interface 함수 작성
        retrofitAPIService.sendScanResult(jsonObject).enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                Log.d(TAG, "onResponse/result: ${response.body()}")
                val resp = response.body()!!
                val referencePoint = resp.referencePoint.toInt()
                Log.d(TAG, "onResponse/referencePoint: $referencePoint")

                // FixMe: 서버 측에서 어떻게 보낼 건지 정한 후 작성해야 함.
//                val mGson = Gson()
//                val result = mGson.fromJson(response.body(), Result::class.java)

                scanResultView.onScanResultSuccess(referencePoint)
            }

            override fun onFailure(call: Call<Result>, t: Throwable) {
                Log.d(TAG, "onFailure")
                scanResultView.onScanResultFailure()
            }
        })
    }
}
