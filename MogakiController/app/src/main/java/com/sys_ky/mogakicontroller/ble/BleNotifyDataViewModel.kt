package com.sys_ky.mogakicontroller.ble

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BleNotifyViewModel(val bleNotifyDataSource: BleNotifyDataSource): ViewModel() {

    fun setBleNotify(newStr: String) {
        return bleNotifyDataSource.setNotifyLiveData(newStr)
    }

    fun clearBleNotify() {
        return bleNotifyDataSource.clearNotifyLiveData()
    }

    fun getBleNotify(): String {
        var rtnStr = ""
        if(bleNotifyDataSource.getNotifyLiveData().value != null) {
            rtnStr = bleNotifyDataSource.getNotifyLiveData().value!!
        }
        return rtnStr
    }

    fun getBleNotifyLiveData(): LiveData<String> {
        return bleNotifyDataSource.getNotifyLiveData()
    }
}

class BleNotifyiewModelFactory() : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BleNotifyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BleNotifyViewModel(
                bleNotifyDataSource = BleNotifyDataSource.getDataSource()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}