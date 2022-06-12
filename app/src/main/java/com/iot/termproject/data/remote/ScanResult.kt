package com.iot.termproject.data.remote

import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("predict") val referencePoint: Int
)
