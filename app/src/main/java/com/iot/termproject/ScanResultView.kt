package com.iot.termproject

import com.iot.termproject.data.remote.Result

interface ScanResultView {
    fun onScanResultSuccess(referencePoint: Int)
    fun onScanResultFailure()
}
