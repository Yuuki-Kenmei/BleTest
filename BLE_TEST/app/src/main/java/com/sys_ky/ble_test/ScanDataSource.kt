package com.sys_ky.ble_test

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ScanDataSource {
    private val scanLiveData = MutableLiveData<List<ScanInfo>>()

    fun setScanLiveData(newList: List<ScanInfo>) {
        scanLiveData.value = newList
    }

    fun getScanLiveData(): LiveData<List<ScanInfo>> {
        return scanLiveData
    }

    companion object {
        private var INSTANCE: ScanDataSource? = null

        fun getDataSource(): ScanDataSource {
            return synchronized(ScanDataSource::class.java) {
                val newInstance = INSTANCE ?: ScanDataSource()
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}