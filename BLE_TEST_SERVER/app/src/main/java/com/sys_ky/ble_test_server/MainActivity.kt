package com.sys_ky.ble_test_server

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*


class MainActivity : AppCompatActivity() {

    var sendCount: Int = 0

    var g_bluetoothManager: BluetoothManager? = null
    var g_bluetoothAdapter: BluetoothAdapter? = null
    var g_bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    var g_bluetoothGattServer: BluetoothGattServer? = null
    var g_bleCharacteristic: BluetoothGattCharacteristic? = null
    var g_bleCharacteristic2: BluetoothGattCharacteristic? = null
    var g_connectDevice: BluetoothDevice? = null

    val service_uuid = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
    val characteristicTx_uuid = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
    val characteristicRx_uuid = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"



    //パーミッション画面起動用
    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        var cancelFlg = false
        result.forEach {
            if(it.value == true) {
            } else {
                cancelFlg = true
                return@forEach
            }
        }
        if(cancelFlg) {
            //NG
            val dialog = AlertDialog.Builder(this)
                .setTitle("権限エラー")
                .setMessage("許可しない限り使えないよ")
                .setPositiveButton(
                    "OK",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                    })
                .show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionArray = arrayListOf<String>()
        if(Build.VERSION.SDK_INT <= 30) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionArray.add(Manifest.permission.BLUETOOTH)
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionArray.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionArray.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionArray.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if(Build.VERSION.SDK_INT > 30) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
        }
        if(permissionArray.size > 0) {
            requestPermissionLauncher.launch(permissionArray.toTypedArray())
        }

        g_bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        g_bluetoothAdapter = g_bluetoothManager?.adapter
        g_bluetoothLeAdvertiser = g_bluetoothAdapter?.bluetoothLeAdvertiser
    }

    @SuppressLint("MissingPermission")
    fun onclick(view: View) {

        //GATTサービス
        val bluetoothGattService = BluetoothGattService(UUID.fromString(service_uuid), BluetoothGattService.SERVICE_TYPE_PRIMARY)

        //GATTキャラクタリスティック
        g_bleCharacteristic = BluetoothGattCharacteristic(
            UUID.fromString(characteristicTx_uuid),
            BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
        )
        bluetoothGattService.addCharacteristic(g_bleCharacteristic)


        g_bleCharacteristic2 = BluetoothGattCharacteristic(
            UUID.fromString(characteristicRx_uuid),
            BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
        )
        bluetoothGattService.addCharacteristic(g_bleCharacteristic2)

        //GATTディスクリプタ
        val dataDescriptor = BluetoothGattDescriptor(
            UUID.fromString(characteristicTx_uuid),
            BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ
        )
        g_bleCharacteristic?.addDescriptor(dataDescriptor)

        //GATTサーバ起動
        g_bluetoothGattServer = g_bluetoothManager?.openGattServer(this, g_callback)
        g_bluetoothGattServer?.addService(bluetoothGattService)

        //アドバタイズのセッティング
        val dataBuilder = AdvertiseData.Builder()
        dataBuilder.setIncludeTxPowerLevel(true)
        dataBuilder.addServiceUuid(ParcelUuid.fromString(service_uuid))
        val settingsBuilder = AdvertiseSettings.Builder()
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)

        g_bluetoothLeAdvertiser = g_bluetoothAdapter?.bluetoothLeAdvertiser
        g_bluetoothLeAdvertiser?.startAdvertising(
            settingsBuilder.build(),
            dataBuilder.build(),
            object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    Toast.makeText(this@MainActivity, "onStartSuccess", Toast.LENGTH_SHORT).show()
                    Log.e("","onStartSuccess")
                }

                override fun onStartFailure(errorCode: Int) {
                    Toast.makeText(this@MainActivity, "onStartFailure", Toast.LENGTH_SHORT).show()
                    Log.e("","onStartFailure")
                }
            })
    }

    //GATTサーバのコールバック
    private val g_callback: BluetoothGattServerCallback= object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            Log.e("", "onConnectionStateChange")
            super.onConnectionStateChange(device, status, newState)
            g_connectDevice = device
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("","接続した")
                sendCount = 0
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("","切断した")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.e("", "onCharacteristicWriteRequest")
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)

            //キャラクタリスティック受信バイト列を文字列に
            var receiveStr = ""
            if(value != null) {
                receiveStr = String(value)
            }

            Log.e("","Characteristic受信：" + receiveStr)

            Handler(Looper.getMainLooper()).post {
                findViewById<TextView>(R.id.textView).text = receiveStr
            }

            //レスポンス
            if(responseNeeded) {
                g_bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.e("", "onDescriptorWriteRequest")
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)

            //ディスクリプタ受信バイト列を文字列に
            var receiveStr = ""
            if(value != null) {
                receiveStr = String(value)
            }

            Log.e("","Descriptor受信：" + receiveStr)

            //レスポンス
            if(responseNeeded) {
                g_bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun btnclick2(view: View) {
        if(g_bleCharacteristic != null && g_bluetoothGattServer != null && g_connectDevice != null) {
            sendCount++
            //notify送信
            //g_bleCharacteristic?.setValue("peripheralSend".toByteArray())
            g_bleCharacteristic?.setValue(sendCount.toString().toByteArray())
            g_bluetoothGattServer?.notifyCharacteristicChanged(g_connectDevice, g_bleCharacteristic, false)

            g_bleCharacteristic2?.setValue(sendCount.toString().toByteArray())
            g_bluetoothGattServer?.notifyCharacteristicChanged(g_connectDevice, g_bleCharacteristic2, false)

            Log.e("","notify送信")
        }
    }

    @SuppressLint("MissingPermission")
    fun btnclick3(view:View) {
        //GATTサーバ切断
        if(g_bluetoothGattServer != null) {
            g_bluetoothGattServer?.close()
            g_bluetoothGattServer = null
            g_bleCharacteristic = null
        }
    }
}






