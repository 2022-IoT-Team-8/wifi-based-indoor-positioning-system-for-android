package com.iot.termproject.data.remote

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

data class ScanResult(
    @SerializedName("scan_result") val scanResult: JsonArray
)

data class Result(
    @SerializedName("reference_point") val referencePoint: Int?
)