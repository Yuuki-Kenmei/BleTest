package com.example.bluetoothtest

import android.app.Application
import android.bluetooth.BluetoothSocket

class global :Application() {
    var g_socket: BluetoothSocket? = null

    companion object {
        private var instance: global? = null

        fun getInstance(): global {
            if(instance==null) {
                instance = global()
            }
            return instance!!
        }
    }
}