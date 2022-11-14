package com.sys_ky.mogakicontroller.ble

import androidx.lifecycle.MutableLiveData

class BleScanDataSource {
    private val scanLiveData = MutableLiveData<List<BlePeripheral>>()

    fun setScanLiveData(newItem: BlePeripheral, index: Int) {
        var newList = scanLiveData.value?.toMutableList()
        newList?.add(index, newItem)
        if(newItem != null) {
            scanLiveData.value = newList!!
        }
    }

    fun clearScanLiveData() {
        scanLiveData.value = listOf<BlePeripheral>()
    }

    fun getScanLiveData(): MutableLiveData<List<BlePeripheral>> {
        return scanLiveData
    }

    companion object {
        private var INSTANCE: BleScanDataSource? = null

        fun getDataSource(): BleScanDataSource {
            return synchronized(BleScanDataSource::class.java) {
                val newInstance = INSTANCE ?: BleScanDataSource()
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}