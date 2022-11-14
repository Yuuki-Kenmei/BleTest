package com.sys_ky.ble_test

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

data class ScanInfo (
    val device: BluetoothDevice,
    val rssi: Int
)