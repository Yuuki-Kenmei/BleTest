package com.sys_ky.ble_test

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class MainActivity : AppCompatActivity() {

    var mBluetoothManager: BluetoothManager? = null
    var mBluetoothAdapter: BluetoothAdapter? = null
    var mScanList: MutableList<ScanInfo> = mutableListOf<ScanInfo>()

    var mBluetoothGatt: BluetoothGatt? = null
    var mBluetoothCharacteristic: BluetoothGattCharacteristic? = null
    val service_uuid = "8C84F705-E99C-F3FB-F496-34ED05441563"
    val characteristic_uuid = "A7CEAC0A-7333-5AF1-EEDE-546663EAF0EA"

    private val scanViewModel by viewModels<ScanViewModel> {
        ScanListViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), 1)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADMIN), 2)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 3)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 4)
        }
        if(Build.VERSION.SDK_INT > 30) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 5)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN), 6)
            }
        }

        mBluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager?.adapter

        val scanAdapter = ScanAdapter{ scan -> adapterOnClick(scan) }
        val recyclerView = findViewById<RecyclerView>(R.id.deviceListView)
        recyclerView.adapter=scanAdapter

        scanViewModel.getScanInfoList().observe(this) {
            it?.let {
                scanAdapter.setScanList(it)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun btnClick(view: View) {

        val bluetoothLeScanner = mBluetoothAdapter?.bluetoothLeScanner

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(500)
            .build()
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                bluetoothLeScanner?.stopScan(mScanCallback)
                Log.e("","stopScan")
            }, 5000)
            mScanList.clear()
            bluetoothLeScanner?.startScan(null, settings, mScanCallback)
            Log.e("","startScan")
        } catch (e: IllegalStateException) {     // Exception if the BT adapter is not on
            Log.e("","error")
        }
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.e("","onScanResult")
            makeScanList(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            Log.e("","onBatchScanResults:" + results.size.toString())
            makeScanList(results)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("","onScanFailed")
        }
    }

    @SuppressLint("NewApi")
    private fun makeScanList(result: ScanResult) {
        val flg = makeScanList_sub(result)
        if(flg) {
            scanViewModel.setScanInfoList(mScanList)
        }
    }

    private fun makeScanList(results: List<ScanResult>) {
        var insCount = 0
        results.forEach { result ->
            val flg = makeScanList_sub(result)
            if(flg) {
                insCount++
            }
        }
        if(insCount > 0) {
            scanViewModel.setScanInfoList(mScanList)
        }
    }

    @SuppressLint("NewApi")
    fun makeScanList_sub(result: ScanResult): Boolean {
        if(result.device == null) {
            return false
        } else {
            mScanList.forEach {
                if(result.device.address == "14:50:51:01:E7:FC") {
                    Log.e("txPower", result.txPower.toString())
                    Log.e("rssi", result.rssi.toString())
                }
                if(it.device.address == result.device.address) {
                    return false
                }
            }
            mScanList.add(ScanInfo(result.device, result.rssi))
            return true
        }
    }

    @SuppressLint("MissingPermission")
    private fun adapterOnClick(scanInfo: ScanInfo) {
        mBluetoothGatt = scanInfo.device.connectGatt(this, false, mGattCallback)
    }

    val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.e("", "onConnectionStateChange")
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("","接続した")
                mBluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("","切断した")
                mBluetoothGatt?.close()
                mBluetoothGatt = null
            }
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            Log.e("", "onServiceChanged")
            super.onServiceChanged(gatt)
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.e("", "onServicesDiscovered")
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service: BluetoothGattService? = gatt?.getService(UUID.fromString(service_uuid))
                if(service != null) {
                    mBluetoothCharacteristic = service.getCharacteristic(UUID.fromString(characteristic_uuid))
                    if(mBluetoothCharacteristic != null) {
                        mBluetoothGatt = gatt

                        val registered = mBluetoothGatt?.setCharacteristicNotification(mBluetoothCharacteristic, true)

                        val descriptor = mBluetoothCharacteristic?.getDescriptor(UUID.fromString(characteristic_uuid))
                        descriptor?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)

                        mBluetoothGatt?.writeDescriptor(descriptor)

                        Log.e("","送信準備した")
                    }
                }
            } else {
                Log.e("","送信準備できなかった:" + status.toString())
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            Log.e("", "onCharacteristicChanged")
            super.onCharacteristicChanged(gatt, characteristic)

            if(characteristic?.value != null) {
                val str = String(characteristic?.value)
                Log.e("", "notify受信：" + str)
            }

        }
    }

    @SuppressLint("MissingPermission")
    fun btnClick2(view: View) {
        if(mBluetoothCharacteristic != null && mBluetoothGatt != null) {
            mBluetoothCharacteristic?.setValue("123456789012345678901234567890".toByteArray())
            mBluetoothGatt?.writeCharacteristic(mBluetoothCharacteristic)
            Log.e("", "送信")
        }
    }

    @SuppressLint("MissingPermission")
    fun btnClick3(view: View) {
        if(mBluetoothGatt != null) {
            mBluetoothGatt?.close()
            mBluetoothGatt = null
            mBluetoothCharacteristic = null
        }
    }
}

