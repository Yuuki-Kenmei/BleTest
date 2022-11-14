package com.example.bluetoothtest_server

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var g_bluetoothAdaptor: BluetoothAdapter? = null
    private var g_socket: BluetoothSocket? = null
    private var g_serverSocket: BluetoothServerSocket? = null

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

        //Accept()
    }

    @SuppressLint("MissingPermission")
    fun Accept(view: View) {

        if(findViewById<TextView>(R.id.textView).text == "未接続") {
            val scope = CoroutineScope(Dispatchers.Default)

            scope.launch {
                Accept()
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun Accept() {
        Handler(Looper.getMainLooper()).post {
            findViewById<TextView>(R.id.textView).text = "接続待ち"
        }

        var shouldLoop = true

        //g_serverSocket = g_bluetoothAdaptor?.listenUsingInsecureRfcommWithServiceRecord("BTTEST", UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb"))
        g_serverSocket = g_bluetoothAdaptor?.listenUsingInsecureRfcommWithServiceRecord("BTTEST", UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))

        while (shouldLoop) {
            try {
                g_socket = g_serverSocket?.accept()
            } catch (e: IOException) {
                shouldLoop = false
            }

            g_socket.also {
                //Toast.makeText(this, "接続しました。", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).post {
                    findViewById<TextView>(R.id.textView).text = "接続済"
                }
                shouldLoop = false
            }
        }

        if(g_socket == null) {
            return
        }


        val inStream: InputStream? = g_socket?.inputStream
        //val mmBuffer: ByteArray = ByteArray(1024)
        val mmBuffer: ByteArray = ByteArray(16)
        while(true) {
            var numBytes = try {
                inStream?.read(mmBuffer)
                Handler(Looper.getMainLooper()).post {
                    findViewById<TextView>(R.id.textView2).text = mmBuffer.toString(Charsets.UTF_8)
                }
                //Toast.makeText(this, mmBuffer.toString(Charsets.UTF_8), Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                break
            }
        }
    }
}