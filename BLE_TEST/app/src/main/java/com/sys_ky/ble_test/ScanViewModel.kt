package com.sys_ky.ble_test

import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.random.Random

class ScanViewModel(val dataSource: ScanDataSource): ViewModel() {

    fun setScanInfoList(newList: List<ScanInfo>) {
        return dataSource.setScanLiveData(newList)
    }

    fun getScanInfoList():LiveData<List<ScanInfo>> {
        return dataSource.getScanLiveData()
    }
}

class ScanListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanViewModel(
                dataSource = ScanDataSource.getDataSource()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}