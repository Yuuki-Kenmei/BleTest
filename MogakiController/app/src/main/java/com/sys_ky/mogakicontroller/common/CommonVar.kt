package com.sys_ky.mogakicontroller.common

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothSocket
import com.sys_ky.mogakicontroller.ble.BlePeripheral

class CommonVar : Application() {
    //固定値
    val c_defaultUUID: String = "00001101-0000-1000-8000-00805F9B34FB"
    val c_defaultUUID_service: String = "8C84F705-E99C-F3FB-F496-34ED05441563"
    val c_defaultUUID_characteristic: String = "A7CEAC0A-7333-5AF1-EEDE-546663EAF0EA"
    val c_defaultSendMax: Int = 100
    val c_defaultThreshold: Int = 5
    val c_method_bluetooth: Int = 1
    val c_method_ble: Int = 2
    val c_configName: String = "settings"
    val c_configKey_connectMethod: String = "connectMethod"
    val c_configKey_sendMax: String = "sendMax"
    val c_configKey_uuid: String = "uuid"
    val c_configKey_uuid_service: String = "uuid_service"
    val c_configKey_uuid_characteristic: String = "uuid_characteristic"
    val c_configKey_xAxisRightPositive: String = "xAxisRightPositive"
    val c_configKey_yAxisRightPositive: String = "yAxisRightPositive"
    val c_configKey_threshold: String = "threshold"

    //変数
    var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothSocket: BluetoothSocket? = null
    var connectMethod: Int = c_method_bluetooth
    var uuid: String = c_defaultUUID
    var uuid_service: String = c_defaultUUID_service
    var uuid_characteristic: String = c_defaultUUID_characteristic
    var sendMax: Int = c_defaultSendMax
    var xAxisRightPositive: Boolean = true
    var yAxisUpPositive: Boolean = true
    var threshold: Int = c_defaultThreshold
    var threshold_array: Array<String> = arrayOf()
    var blePeripheral: BlePeripheral? = null

    companion object {
        private var instance: CommonVar? = null

        fun getInstance(): CommonVar {
            if(instance ==null) {
                instance = CommonVar()
            }
            return instance!!
        }
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}