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
    companion object {
        private const val TAG = "SERVICE/SCAN-RESULT"
    }

    // parameter로 scanResult가 아니라 jsonObject로 변경하였습니다.
    fun scanResult(scanResultView: ScanResultView, jsonObject: JsonObject) {
        val retrofitAPIService = retrofit.create(RetrofitAPI::class.java);

        // interface 함수 작성
        retrofitAPIService.sendScanResult(jsonObject).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d(TAG, "onResponse/response.isSuccessful: ${response.isSuccessful}")
                val resp = response.body()!!
                Log.d(TAG, "onResponse/resp: $resp")

                // 1.
//                val referencePoint = resp.referencePoint

                // 2.
                val mGson = Gson()
                val referencePoint = mGson.fromJson(resp, Result::class.java).referencePoint

                Log.d(TAG, "onResponse/referencePoint: $referencePoint")

                scanResultView.onScanResultSuccess(referencePoint)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "onFailure")
                scanResultView.onScanResultFailure()
            }
        })
    }
}
