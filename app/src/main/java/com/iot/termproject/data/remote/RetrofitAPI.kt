package com.iot.termproject.data.remote

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface RetrofitAPI {

    @POST("/predict")
    fun sendScanResult(
        @Body jsonObject: JsonObject
    ): Call<JsonObject>

}
