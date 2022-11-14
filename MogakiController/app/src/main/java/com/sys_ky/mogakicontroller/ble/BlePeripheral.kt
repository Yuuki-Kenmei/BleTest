package com.sys_ky.mogakicontroller.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sys_ky.mogakicontroller.MainActivity
import com.sys_ky.mogakicontroller.common.CommonVar
import java.util.*

class BlePeripheral constructor(scanResult: ScanResult){

    private var mCommonVar: CommonVar = CommonVar.getInstance()

    private var mScanResult: ScanResult? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null

    private var mConnectionState: Int =  BluetoothProfile.STATE_DISCONNECTED

    private val mBluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            //Log.d("", "onConnectionStateChange")
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Log.d("", "接続した")
                mBluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //Log.d("", "切断した")
                disconnectGatt()
            }
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            //Log.d("", "onServiceChanged")
            super.onServiceChanged(gatt)
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            //Log.d("", "onServicesDiscovered")
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service: BluetoothGattService? =
                    gatt?.getService(UUID.fromString(mCommonVar.uuid_service))
                if (service != null) {
                    mBluetoothGattCharacteristic =
                        service.getCharacteristic(UUID.fromString(mCommonVar.uuid_characteristic))
                    if (mBluetoothGattCharacteristic != null) {
                        mBluetoothGatt = gatt

                        val registered =
                            mBluetoothGatt?.setCharacteristicNotification(
                                mBluetoothGattCharacteristic,
                                true
                            )

                        val descriptor =
                            mBluetoothGattCharacteristic?.getDescriptor(
                                UUID.fromString(mCommonVar.uuid_characteristic)
                            )
                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                        //mBluetoothGatt?.writeDescriptor(descriptor)

                        //Log.d("", "送信準備した")

                        mConnectionState = BluetoothProfile.STATE_CONNECTED
                        mCommonVar.blePeripheral = this@BlePeripheral
                        setBleNotify("ready")
                    }
                    else {
                        setBleNotify("service_null")
                    }
                }
                else {
                    setBleNotify("service_null")
                }
            } else {
                setBleNotify("gatt_failed")
                //Log.d("", "送信準備できなかった:" + status.toString())
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            //Log.d("", "onCharacteristicChanged")
            super.onCharacteristicChanged(gatt, characteristic)

            if (characteristic?.value != null) {
                val str = String(characteristic.value)
                //Log.d("", "notify受信：" + str)
                setBleNotify(str)
            }
        }
    }

    private fun setBleNotify(str: String) {
        Handler(Looper.getMainLooper()).post {
            MainActivity.gBleNotifyViewModel!!.setBleNotify(str)
        }
    }

    init {
        mScanResult = scanResult
    }

    @SuppressLint("MissingPermission")
    fun connectGatt(context: Context) {
        mBluetoothGatt = mScanResult?.device?.connectGatt(context, false, mBluetoothGattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnectGatt() {
        mBluetoothGatt?.disconnect()
        mBluetoothGatt?.close()
        mBluetoothGatt = null
        mBluetoothGattCharacteristic = null
        mConnectionState = BluetoothProfile.STATE_DISCONNECTED
    }

    fun getDevice(): BluetoothDevice? {
        return mScanResult?.device
    }

    fun getRssi(): Int {
        var rtnValue: Int = -100
        if(mScanResult != null) {
            rtnValue = mScanResult!!.rssi
        }
        return rtnValue
    }

    @SuppressLint("MissingPermission")
    fun getName(): String {
        var rtnValue: String? = ""
        if(mScanResult?.device?.name.isNullOrEmpty()) {
            rtnValue = mScanResult?.scanRecord?.deviceName
        } else {
            rtnValue = mScanResult?.device?.name
        }

        if(rtnValue == null) {
            rtnValue = ""
        }
        return rtnValue.toString()
    }

    fun getAddress(): String {
        var rtnValue: String? = ""
        if(!mScanResult?.device?.address.isNullOrEmpty()) {
            rtnValue = mScanResult?.device?.address
        }
        return rtnValue.toString()
    }

    fun getConnectionState(): Int {
        return mConnectionState
    }

    fun isConnected(): Boolean {
        return mConnectionState == BluetoothProfile.STATE_CONNECTED
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(send: ByteArray) {
        mBluetoothGattCharacteristic?.setValue(send)
        mBluetoothGatt?.writeCharacteristic(mBluetoothGattCharacteristic)
    }
}