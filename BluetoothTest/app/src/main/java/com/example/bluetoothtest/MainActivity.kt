package com.example.bluetoothtest

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {

    private var g_bluetoothAdaptor: BluetoothAdapter? = null
    private var g_bluetoothDevice: BluetoothDevice? = null
    private var g_uuid: UUID? = null
    //private var g_socket: BluetoothSocket? = null

    private val g_reg_Bluetooth_Active =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            if (result.resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "BluetoothがOnにされなかったため、終了します。", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        g_bluetoothAdaptor = bluetoothManager.getAdapter()
        //Bluetoothがサポートされていない場合、終了
        if(g_bluetoothAdaptor == null) {
            Toast.makeText(this, "Bluetoothがサポートされていないため、終了します。", Toast.LENGTH_SHORT).show()
            finish()
        }

        //BluetoothがOnでない場合、起動確認
        if(g_bluetoothAdaptor?.isEnabled == false) {
            g_reg_Bluetooth_Active.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        //findViewById<EditText>(R.id.editText_Mac).setText("F0:9E:4A:82:C9:B2")
        findViewById<EditText>(R.id.editText_Mac).setText("3C:01:EF:60:68:84")
    }

    override fun onDestroy() {
        super.onDestroy()

        //if(g_socket != null) {
        //    g_socket?.close()
        //}
        if(global.getInstance().g_socket != null) {
            global.getInstance().g_socket?.close()
        }
    }

    fun connectPairDevice(view: View) {
        //BDアドレス取得
        val bdAddress: String = findViewById<EditText>(R.id.editText_Mac).text.toString()
        if(bdAddress.equals("")) {
            Toast.makeText(this, "MACアドレスを入力してね。", Toast.LENGTH_SHORT).show()
            return
        }

        //ペアリング済のデバイス一覧
        val pairedDevices: Set<BluetoothDevice>? = g_bluetoothAdaptor?.bondedDevices
        pairedDevices?.forEach { device ->
            //ペアリング済デバイスのうち、指定のBDアドレスと一致した場合、接続しに行く
            if(bdAddress.equals(device.address)) {
                g_bluetoothDevice = device
                g_uuid = device.uuids[0].uuid
                return@forEach
            }
        }

        Connect()
    }

    fun Connect() {

        g_bluetoothAdaptor?.cancelDiscovery()

        if(g_bluetoothDevice == null) {
            return
        }

        //g_socket = g_bluetoothDevice?.createInsecureRfcommSocketToServiceRecord(g_uuid)
        //g_socket = g_bluetoothDevice?.createInsecureRfcommSocketToServiceRecord(UUID.fromString("41eb5f39-6c3a-4067-8bb9-bad64e6e0908"))
        global.getInstance().g_socket = g_bluetoothDevice?.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        //if (g_socket == null) {
        //    return
        //}
        if (global.getInstance().g_socket == null) {
            return
        }

        try {
            //g_socket?.connect()
            global.getInstance().g_socket?.connect()
        }catch (e: Exception)
        {
            Toast.makeText(this, "接続失敗", Toast.LENGTH_SHORT).show()
        }

        //g_socket.also {
        global.getInstance().g_socket.also {
            Toast.makeText(this, "接続しました。", Toast.LENGTH_SHORT).show()
        }
    }

    fun SendMessage(view: View) {

        val sendText: String = findViewById<EditText>(R.id.editText_Send).text.toString()
        if(String.equals("")) {
            Toast.makeText(this, "送信テキストを入力してね。", Toast.LENGTH_SHORT).show()
            return
        }

        //g_socket?.outputStream?.write(sendText.toByteArray())
        //g_socket?.outputStream?.flush()
        global.getInstance().g_socket?.outputStream?.write(sendText.toByteArray())
        global.getInstance().g_socket?.outputStream?.flush()
    }

}