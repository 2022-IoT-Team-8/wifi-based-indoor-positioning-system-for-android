package com.iot.termproject

interface ScanResultView {
    fun onScanResultSuccess(referencePoint: Int)
    fun onScanResultFailure()
}
