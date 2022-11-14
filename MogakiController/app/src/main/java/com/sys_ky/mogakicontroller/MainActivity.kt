package com.sys_ky.mogakicontroller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.view.Gravity
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Fade
import androidx.transition.Slide
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.sys_ky.mogakicontroller.ble.BleNotifyViewModel
import com.sys_ky.mogakicontroller.ble.BleNotifyiewModelFactory
import com.sys_ky.mogakicontroller.common.CommonVar
import com.sys_ky.mogakicontroller.control.StickView
import java.math.BigDecimal
import java.util.*

const val RETURN_DEVICE_NAME = "com.sys_ky.mogakicontroller.RETURN_DEVICE_NAME"
const val RETURN_DEVICE_ADDRESS = "com.sys_ky.mogakicontroller.RETURN_DEVICE_ADDRESS"

class MainActivity : AppCompatActivity()
    , Fragment_Title.OnTouchListener
    , Fragment_Menu.OnTouchListener
    , Fragment_Menu.OnClickListener
    , Fragment_StickController.OnTouchListener
    , Fragment_BluetoothList.OnItemClickListener {

    private var mCommonVar: CommonVar = CommonVar.getInstance()
    //フラグメント変数
    val mFragment_title: Fragment_Title = Fragment_Title()
    val mFragment_menu: Fragment_Menu = Fragment_Menu()
    val mFragment_stickController: Fragment_StickController = Fragment_StickController()
    val mFragment_BleList: Fragment_BleList = Fragment_BleList()
    val mFragment_BluetoothList: Fragment_BluetoothList = Fragment_BluetoothList()

    //タイトル表示関連変数
    //タイトル表示を行うか
    var mShowTitleFlg: Boolean = true
    //現在タイトルが表示されているか
    var mShowedTitleFlg: Boolean = false
    //タイトル表示タイマー
    val mTitleTimer = object : CountDownTimer(3000, 3000) {
        override fun onTick(millisUntilFinished: Long) {
        }
        override fun onFinish() {
            closeTitle()
        }
    }

    //スティック関連変数
    private var mStartPosX_Left: Float = 0f
    private var mStartPosY_Left: Float = 0f
    private var mStartPosX_Right: Float = 0f
    private var mStartPosY_Right: Float = 0f

    private var mLatest_movePerX: Float = 0f
    private var mLatest_movePerY: Float = 0f

    //QR画面遷移用
    val mQrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            Toast.makeText(this, result.contents, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.toast_cancel), Toast.LENGTH_SHORT).show()
        }
    }

    //ヘルプ画面遷移用
    val mHelpLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
    }

    //設定画面遷移用
    val mSettingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        //設定画面から戻ったときは必ず設定を読み直す
        readConfig()
        //設定値を画面に反映
        refInfoOnDisplay("", "")
    }

    //Bluetooth起動確認起動用
    val mBluetoothActiveLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, getString(R.string.toast_bluetooth_not), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    //パーミッション画面起動用
    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(),) { result ->
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
                .setTitle(R.string.alert_title_error)
                .setMessage(R.string.alert_message_permission_ng)
                .setPositiveButton(
                    R.string.alert_button_ok,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        finish()
                    })
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //タイトル表示用変数だけインスタンス保存
        if(null != savedInstanceState) {
            mShowTitleFlg = savedInstanceState.getBoolean("g_showTitleFlg")
            mShowedTitleFlg = savedInstanceState.getBoolean("g_showedTitleFlg")
        }

        //ツールバーの設定
        var toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        //メニュー画面フラグメントの設定
        val slide = Slide()
        slide.slideEdge = Gravity.LEFT
        mFragment_menu.enterTransition = slide
        mFragment_menu.exitTransition = slide

        //タイトル画面フラグメントの設定
        val fade = Fade()
        fade.duration = 500
        mFragment_title.enterTransition = fade
        mFragment_title.exitTransition = fade

        //スティックコントローラー画面フラグメントの設定
        val slide2 = Slide()
        slide2.slideEdge = Gravity.BOTTOM
        mFragment_stickController.enterTransition = slide2
        mFragment_stickController.exitTransition = slide2

        //切断ボタン
        val button_disconnect = findViewById<Button>(R.id.main_button_disconnect)
        button_disconnect.setOnClickListener(View.OnClickListener {
            disconnectButtonClick()
        })

        //ペリフェラル通知受信用ビューモデル
        gBleNotifyViewModel = ViewModelProvider(this, BleNotifyiewModelFactory()).get(
            BleNotifyViewModel::class.java)
        gBleNotifyViewModel!!.getBleNotifyLiveData().observe(this) {
            it?.let {
                if(it == "ready") {

                    if(mCommonVar.blePeripheral != null) {
                        //BLEリスト画面を閉じる
                        closeBleList()

                        Toast.makeText(this, getString(R.string.toast_connect), Toast.LENGTH_SHORT).show()

                        //現在の情報で画面に反映
                        refInfoOnDisplay(mCommonVar.blePeripheral!!.getName(), mCommonVar.blePeripheral!!.getAddress())

                        // スティック表示
                        showStick()
                    }
                }
                if(it == "service_null") {
                    val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.alert_title_error)
                        .setMessage(R.string.alert_message_ble_connect_error_1)
                        .setPositiveButton(
                            R.string.alert_button_ok,
                            DialogInterface.OnClickListener { dialogInterface, i -> })
                        .show()
                }
                if(it == "gatt_failed") {
                    val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.alert_title_error)
                        .setMessage(R.string.alert_message_ble_connect_error_2)
                        .setPositiveButton(
                            R.string.alert_button_ok,
                            DialogInterface.OnClickListener { dialogInterface, i -> })
                        .show()
                }

                mFragment_BleList.mTryConnectFlg = false

                Handler(Looper.getMainLooper()).post {
                    gBleNotifyViewModel!!.clearBleNotify()
                }

                if(!it.isNullOrEmpty() && it != "ready" && it != "service_null" && it != "gatt_failed") {
                    Toast.makeText(this,"ペリフェラルからの通知:" + gBleNotifyViewModel!!.getBleNotify(), Toast.LENGTH_SHORT).show()
                }
            }
        }

        //設定値を読み込み
        readConfig()

        //設定値を画面に反映
        refInfoOnDisplay("", "")

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mCommonVar.bluetoothAdapter = bluetoothManager.adapter

        //タイトル表示
        if(mShowTitleFlg) {
            showTitle()
            mShowTitleFlg = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        val connected_bluetooth = mCommonVar.connectMethod == mCommonVar.c_method_bluetooth && mCommonVar.bluetoothSocket?.isConnected == true
        val connected_ble = mCommonVar.connectMethod == mCommonVar.c_method_ble && mCommonVar.blePeripheral?.isConnected() == true
        if(connected_bluetooth) {
            mCommonVar.bluetoothSocket?.close()
        }
        if(connected_ble) {
            mCommonVar.blePeripheral?.disconnectGatt()
        }
    }


    //切断ボタン押下時
    @SuppressLint("MissingPermission")
    private fun disconnectButtonClick() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.alert_title_confirm)
            .setMessage(R.string.alert_message_confirm_disconnect)
            .setPositiveButton(
                R.string.alert_button_yes,
                DialogInterface.OnClickListener { dialogInterface, i ->
                    try {
                        val connected_bluetooth = mCommonVar.connectMethod == mCommonVar.c_method_bluetooth && mCommonVar.bluetoothSocket?.isConnected == true
                        val connected_ble = mCommonVar.connectMethod == mCommonVar.c_method_ble && mCommonVar.blePeripheral?.isConnected() == true
                        if(connected_bluetooth) {
                            mCommonVar.bluetoothSocket?.close()
                        }
                        if(connected_ble) {
                            mCommonVar.blePeripheral?.disconnectGatt()
                        }

                    } catch(E: Exception) {

                    }
                    Toast.makeText(this, getString(R.string.toast_disconnect), Toast.LENGTH_SHORT).show()

                    refInfoOnDisplay("","")

                    //スティックを閉じる
                    closeStick()
                })
            .setNegativeButton(
                R.string.alert_button_no,
                DialogInterface.OnClickListener { dialogInterface, i ->
                })
            .show()
    }

    //設定値や接続情報を画面に反映する
    private fun refInfoOnDisplay(deviceName: String, deviceAddress: String) {
        if(mCommonVar.connectMethod == mCommonVar.c_method_bluetooth) {
            findViewById<TextView>(R.id.main_textView_method).text = getResources().getString(R.string.setting_method_toggle_bluetooth)
        } else if(mCommonVar.connectMethod == mCommonVar.c_method_ble) {
            findViewById<TextView>(R.id.main_textView_method).text = getResources().getString(R.string.setting_method_toggle_ble)
        }

        val connected_bluetooth = mCommonVar.connectMethod == mCommonVar.c_method_bluetooth && mCommonVar.bluetoothSocket?.isConnected == true
        val connected_ble = mCommonVar.connectMethod == mCommonVar.c_method_ble && mCommonVar.blePeripheral?.isConnected() == true
        if(connected_bluetooth || connected_ble) {
            findViewById<TextView>(R.id.main_textView_state).text = getResources().getString(R.string.main_state_connect)
            findViewById<TextView>(R.id.main_button_disconnect).isEnabled = true
            findViewById<TextView>(R.id.main_button_disconnect).setTextColor(Color.BLACK)
        } else {
            findViewById<TextView>(R.id.main_textView_state).text = getResources().getString(R.string.main_state_disconnect)
            findViewById<TextView>(R.id.main_button_disconnect).isEnabled = false
            findViewById<TextView>(R.id.main_button_disconnect).setTextColor(Color.LTGRAY)
        }

        if(deviceName != null) {
            findViewById<TextView>(R.id.main_textView_connectName).text =deviceName
        }
        if(deviceAddress != null) {
            findViewById<TextView>(R.id.main_textView_connectAddress).text =deviceAddress
        }

        findViewById<TextView>(R.id.main_textView_left_sendX).text = "0.0"
        findViewById<TextView>(R.id.main_textView_left_sendY).text = "0.0"
        findViewById<TextView>(R.id.main_textView_right_sendX).text = "0.0"
        findViewById<TextView>(R.id.main_textView_right_sendY).text = "0.0"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->  {
                showMenu()

                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    //タイトル画面フラグメントを表示する
    private fun showTitle() {
        if(!mShowedTitleFlg) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            //fragmentTransaction.replace(R.id.main_fragmentContainer, g_fragment_title)
            if(fragmentManager.findFragmentById(mFragment_title.id) != null) {
                fragmentTransaction.remove(mFragment_title)
            }
            fragmentTransaction.add(R.id.main_fragmentContainer, mFragment_title)
            //fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            //自動で閉じるため、タイマーを起動
            mTitleTimer.start()
            mShowedTitleFlg = true
        }
    }

    //タイトル画面フラグメントを閉じる
    private fun closeTitle() {
        if(mShowedTitleFlg) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.remove(mFragment_title)
            fragmentTransaction.commit()

            //タイマーは停止
            mTitleTimer.cancel()
            mShowedTitleFlg = false

            //Bluetoothがサポートされていない場合、終了
            if(mCommonVar.bluetoothAdapter == null) {
                val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title_error)
                    .setMessage(R.string.alert_message_bluetooth_notsuport)
                    .setPositiveButton(
                        R.string.alert_button_ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                        })
                    .show()
                finish()
            }

            //BluetoothがOnでない場合、起動確認
            if(mCommonVar.bluetoothAdapter?.isEnabled == false) {
                mBluetoothActiveLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }

            //パーミッション
            val permissionArray = arrayListOf<String>()
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(Manifest.permission.BLUETOOTH)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(Manifest.permission.BLUETOOTH_ADMIN)
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
            }
            if(permissionArray.size > 0) {
                requestPermissionLauncher.launch(permissionArray.toTypedArray())
            }
        }
    }

    //メニュー画面フラグメントを表示する
    private fun showMenu() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        //fragmentTransaction.replace(R.id.main_fragmentContainer, g_fragment_menu)
        if(fragmentManager.findFragmentById(mFragment_menu.id) != null) {
            fragmentTransaction.remove(mFragment_menu)
        }
        fragmentTransaction.add(R.id.main_fragmentContainer, mFragment_menu)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    //メニュー画面フラグメントを閉じる
    private fun closeMenu() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(mFragment_menu)
        fragmentTransaction.commit()
        fragmentManager.popBackStack()
    }

    //BLEリスト画面フラグメントを表示する
    private fun showBleList() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        //fragmentTransaction.replace(R.id.main_fragmentContainer, g_fragment_menu)
        if(fragmentManager.findFragmentById(mFragment_BleList.id) != null) {
            fragmentTransaction.remove(mFragment_BleList)
        }
        fragmentTransaction.add(R.id.main_fragmentContainer, mFragment_BleList)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    //BLEリスト画面フラグメントを閉じる
    private fun closeBleList() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(mFragment_BleList)
        fragmentTransaction.commit()
        fragmentManager.popBackStack()
    }

    //Bluetoothリスト画面フラグメントを表示する
    private fun showBluetoothList() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        //fragmentTransaction.replace(R.id.main_fragmentContainer, g_fragment_menu)
        if(fragmentManager.findFragmentById(mFragment_BluetoothList.id) != null) {
            fragmentTransaction.remove(mFragment_BluetoothList)
        }
        fragmentTransaction.add(R.id.main_fragmentContainer, mFragment_BluetoothList)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    //Bluetoothリスト画面フラグメントを閉じる
    private fun closeBluetoothList() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(mFragment_BluetoothList)
        fragmentTransaction.commit()
        fragmentManager.popBackStack()
    }

    //スティックコントローラー画面フラグメントを表示する
    private fun showStick() {
        val connected_bluetooth = mCommonVar.connectMethod == mCommonVar.c_method_bluetooth && mCommonVar.bluetoothSocket?.isConnected == true
        val connected_ble = mCommonVar.connectMethod == mCommonVar.c_method_ble && mCommonVar.blePeripheral?.isConnected() == true
        if(connected_bluetooth || connected_ble) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            //fragmentTransaction.replace(R.id.main_fragmentContainer, g_fragment_stickcontroller)
            if(fragmentManager.findFragmentById(mFragment_stickController.id) != null) {
                fragmentTransaction.remove(mFragment_stickController)
            }
            fragmentTransaction.add(R.id.main_fragmentContainer, mFragment_stickController)
            //fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    //メニュー画面フラグメントを閉じる
    private fun closeStick() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(mFragment_stickController)
        fragmentTransaction.commit()
    }

    //バックボタン押下時
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if(fragmentManager.findFragmentById(mFragment_title.id) != null) {
            //タイトル表示中は何もしない
            return
        }
        else if(fragmentManager.findFragmentById(mFragment_menu.id) != null) {
            //メニュー表示中は通常動作（前に戻る）
            super.onBackPressed()
            return
        }
        else if(fragmentManager.findFragmentById(mFragment_BleList.id) != null) {
            //BLEリスト表示中は通常動作（前に戻る）
            super.onBackPressed()
            return
        }
        else if(fragmentManager.findFragmentById(mFragment_BluetoothList.id) != null) {
            //Bluetoothリスト表示中は通常動作（前に戻る）
            super.onBackPressed()
            return
        }
        else if(fragmentManager.findFragmentById(mFragment_stickController.id) != null) {
            //スティック表示中は終了
            endActivity()
            return
        }
        else {
            //通常時は終了
            endActivity()
            return
        }
    }

    //確認ダイアログ後、Activityを終了する
    private fun endActivity() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.alert_title_confirm)
            .setMessage(R.string.alert_message_confirm_end)
            .setPositiveButton(
                R.string.alert_button_yes,
                DialogInterface.OnClickListener { dialogInterface, i ->
                    finish()
                })
            .setNegativeButton(
                R.string.alert_button_no,
                DialogInterface.OnClickListener { dialogInterface, i ->
                })
            .show()
    }

    //region フラグメント側の関数の実装
    //タイトル画面フラグメントのタッチ時
    override fun onTouchTitle(view: View, motionEvent: MotionEvent): Boolean {
        when(motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                //画面タッチでタイトル画面フラグメントを閉じる
                closeTitle()
            }
            else -> {

            }
        }
        return true
    }

    //タイトル画面フラグメントの背景タッチ時
    override fun onTouchMenuBackground(view: View, motionEvent: MotionEvent): Boolean {
        when(motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                //メニュー部以外をタッチででメニュー画面フラグメントを閉じる
                closeMenu()
            }
            else -> {

            }
        }
        return true
    }

    //メニュー画面フラグメントのボタン押下時
    @SuppressLint("MissingPermission")
    override fun onClickMenuButton(id: Int) {
        when(id) {
            //接続
            R.id.menu_button_connect -> {
                //メニューは閉じる
                closeMenu()

                val disconnect_bluetooth = mCommonVar.connectMethod == mCommonVar.c_method_bluetooth && mCommonVar.bluetoothSocket?.isConnected == true
                val disconnect_ble = mCommonVar.connectMethod == mCommonVar.c_method_ble && mCommonVar.blePeripheral?.isConnected() == true
                if(disconnect_bluetooth || disconnect_ble) {
                    val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.alert_title_confirm)
                        .setMessage(R.string.alert_message_confirm_newconnect)
                        .setPositiveButton(
                            R.string.alert_button_yes,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                try {
                                    if(disconnect_bluetooth) {
                                        mCommonVar.bluetoothSocket?.close()
                                    }
                                    if(disconnect_ble) {
                                        mCommonVar.blePeripheral?.disconnectGatt()
                                    }
                                } catch(E: Exception) {

                                }
                                Toast.makeText(this, getString(R.string.toast_disconnect), Toast.LENGTH_SHORT).show()

                                //スティックは閉じる
                                closeStick()

                                if(mCommonVar.connectMethod == mCommonVar.c_method_bluetooth) {
                                    //Bluetoothリスト画面を開く
                                    showBluetoothList()
                                } else {
                                    //BLEリスト画面を開く
                                    showBleList()
                                }
                            })
                        .setNegativeButton(
                            R.string.alert_button_no,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                return@OnClickListener
                            })
                        .show()
                }
                else {
                    //スティックは閉じる
                    closeStick()

                    if(mCommonVar.connectMethod == mCommonVar.c_method_bluetooth) {
                        //Bluetoothリスト画面を開く
                        showBluetoothList()
                    } else {
                        //BLEリスト画面を開く
                        showBleList()
                    }
                }
            }
            //QR
            R.id.menu_button_qr -> {
                //メニューは閉じる
                closeMenu()

                //QR画面起動
                val options = ScanOptions()
                options.setOrientationLocked(false)
                mQrLauncher.launch(options)
            }
            //設定
            R.id.menu_button_setting -> {
                //メニューは閉じる
                closeMenu()

                //設定画面へ遷移
                val intent = Intent(this, SettingActivity::class.java)
                mSettingLauncher.launch(intent)
            }
            //ヘルプ
            R.id.menu_button_help -> {
                //メニューは閉じる
                closeMenu()

                //ヘルプ画面へ遷移
                val intent = Intent(this, HelpActivity::class.java)
                mHelpLauncher.launch(intent)
            }
            else -> {

            }
        }
    }

    //Bluetoothリスト画面の一覧選択時
    override fun onItemClickBluetoothList(pos: Int) {
        //一応検索は中止にしておく
        if(this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            mCommonVar.bluetoothAdapter?.cancelDiscovery()
        }

        //選択位置から対象デバイスを取得
        val device = mFragment_BluetoothList.g_pairedDevices?.elementAt(pos)
        if(Build.VERSION.SDK_INT <= 30 || this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {

            //ソケット取得
            mCommonVar.bluetoothSocket = device?.createInsecureRfcommSocketToServiceRecord(
                UUID.fromString(mCommonVar.uuid))

            if(mCommonVar.bluetoothSocket == null) {
                //接続失敗
                val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title_error)
                    .setMessage(R.string.alert_message_bluetooth_connect_error_1)
                    .setPositiveButton(
                        R.string.alert_button_ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                        })
                    .show()
                return
            }

            try {
                //接続
                mCommonVar.bluetoothSocket?.connect()
            } catch (e: java.lang.Exception) {
                //接続失敗
                val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title_error)
                    .setMessage(R.string.alert_message_bluetooth_connect_error_2)
                    .setPositiveButton(
                        R.string.alert_button_ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                        })
                    .show()
                return
            }

            //接続結果を見る
            if(mCommonVar.bluetoothSocket?.isConnected == true) {

                //フラグメントを閉じる
                closeBluetoothList()

                Toast.makeText(this, getString(R.string.toast_connect), Toast.LENGTH_SHORT).show()

                //現在の情報で画面に反映
                refInfoOnDisplay(device?.name.toString(), device?.address.toString())

                // スティック表示
                showStick()
            }
            else {
                //接続失敗
                val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title_error)
                    .setMessage(R.string.alert_message_bluetooth_connect_error_2)
                    .setPositiveButton(
                        R.string.alert_button_ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                        })
                    .show()
                return
            }
        }
    }

    //スティック画面のスティックタッチ時
    override fun onTouchStick(view: StickView, motionEvent: MotionEvent): Boolean {

        //左右どちらのスティックか
        var LR: Int = 0
        if(view.id == R.id.main_stickView_left) {
            LR = 1
        }
        else if(view.id == R.id.main_stickView_right) {
            LR = 2
        }

        when(motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                //タッチ開始位置を記録してそこを0地点に
                if(LR == 1) {
                    mStartPosX_Left = motionEvent.x
                    mStartPosY_Left = motionEvent.y
                }
                else if(LR == 2) {
                    mStartPosX_Right = motionEvent.x
                    mStartPosY_Right = motionEvent.y
                }

                //スティックを0地点へ
                view.moveStick(0f, 0f)

                //0送信
                sendMoveValue(LR, view.getMovePercent(), motionEvent.action)
            }
            MotionEvent.ACTION_MOVE -> {
                //0地点からの移動距離を送信値に
                var nowPosX = motionEvent.x
                var nowPosY = motionEvent.y
                var sabunX: Float = 0f
                var sabunY: Float = 0f
                if(LR == 1) {
                    sabunX = nowPosX - mStartPosX_Left
                    sabunY = nowPosY - mStartPosY_Left
                }
                else if(LR == 2) {
                    sabunX = nowPosX - mStartPosX_Right
                    sabunY = nowPosY - mStartPosY_Right
                }

                //スティックを移動距離へ
                view.moveStick(sabunX, sabunY)

                //移動距離を送信
                sendMoveValue(LR, view.getMovePercent(), motionEvent.action)
            }
            MotionEvent.ACTION_UP -> {
                //タッチ終了で0地点へ
                if(LR == 1) {
                    mStartPosX_Left = 0f
                    mStartPosY_Left = 0f
                }
                else if(LR == 2) {
                    mStartPosX_Right = 0f
                    mStartPosY_Right = 0f
                }

                //スティックを0地点へ
                view.moveStick(0f, 0f)

                //0送信
                //UPは描画よりも先にここまで流れてしまうので固定設定
                val data = FloatArray(2)
                data[0] = 0f
                data[1] = 0f
                sendMoveValue(LR,  data, motionEvent.action)
            }
            else -> {

            }
        }
        return true
    }

    //endregion

    //送信値を設定から計算して送信かつ画面にも表示
    @SuppressLint("MissingPermission")
    private fun sendMoveValue(LR: Int, movePerArray: FloatArray, action: Int) {

        //X軸Y軸移動率
        var movePerX = movePerArray[0]
        var movePerY = movePerArray[1]

        //前回送信値からで設定閾値以上の変化がなければ送信しない
        val threshold: Float = mCommonVar.threshold.toFloat() / 100f
        if(action != MotionEvent.ACTION_MOVE || Math.abs(mLatest_movePerX - movePerX) >= threshold || Math.abs(mLatest_movePerY - movePerY) >= threshold) {
            mLatest_movePerX = movePerX
            mLatest_movePerY = movePerY
        }
        else {
            return
        }

        //送信値計算用の設定値を取得
        val sendMax = mCommonVar.sendMax
        val xAxisRightPositive = mCommonVar.xAxisRightPositive
        val yAxisUpPositive = mCommonVar.yAxisUpPositive

        //X軸Y軸の反転
        if(!xAxisRightPositive) {
            movePerX *= -1
        }
        if(!yAxisUpPositive) {
            movePerY *= -1
        }

        //移動率 * 送信最大値を「0000.00」形式に変換
        val bx = BigDecimal((Math.abs(movePerX * sendMax)).toString())
        val ix = bx.toInt()
        val dx = bx.subtract(BigDecimal(ix)).toFloat()
        val by = BigDecimal((Math.abs(movePerY * sendMax)).toString())
        val iy = by.toInt()
        val dy = by.subtract(BigDecimal(iy)).toFloat()

        var sendValueX = String.format("%04d", ix) + String.format("%.2f", dx).substring(2)
        var sendValueY = String.format("%04d", iy) + String.format("%.2f", dy).substring(2)

        var positiveXstr: String
        var positiveYstr: String
        if(movePerX >= 0) {
            sendValueX = "0" + sendValueX
            positiveXstr = "+"
        }
        else {
            sendValueX = "1" + sendValueX
            positiveXstr = "-"
        }
        if(movePerY >= 0) {
            sendValueY = "0" + sendValueY
            positiveYstr = "+"
        }
        else {
            sendValueY = "1" + sendValueY
            positiveYstr = "-"
        }

        //[!固定1桁][LR1桁][X送信符号1桁][X送信整数4桁小数2桁小数点無しで6桁][Y送信符号1桁][Y送信整数4桁小数2桁小数点無しで6桁]の16バイトで送信
        val sendText = "!" + LR.toString() + sendValueX + sendValueY

        val connected_bluetooth = mCommonVar.connectMethod == mCommonVar.c_method_bluetooth && mCommonVar.bluetoothSocket?.isConnected == true
        val connected_ble = mCommonVar.connectMethod == mCommonVar.c_method_ble && mCommonVar.blePeripheral?.isConnected() == true
        if(connected_bluetooth) {
            mCommonVar.bluetoothSocket?.outputStream?.write(sendText.toByteArray())
            mCommonVar.bluetoothSocket?.outputStream?.flush()
        }
        if(connected_ble) {
            mCommonVar.blePeripheral?.writeCharacteristic(sendText.toByteArray())
        }

        //画面表示
        if(LR == 1) {
            findViewById<TextView>(R.id.main_textView_left_sendX).text = positiveXstr + String.format("%d", ix) + String.format("%.2f", dx).substring(1)
            findViewById<TextView>(R.id.main_textView_left_sendY).text = positiveYstr + String.format("%d", iy) + String.format("%.2f", dy).substring(1)
        }
        else if(LR == 2) {
            findViewById<TextView>(R.id.main_textView_right_sendX).text = positiveXstr + String.format("%d", ix) + String.format("%.2f", dx).substring(1)
            findViewById<TextView>(R.id.main_textView_right_sendY).text = positiveYstr + String.format("%d", iy) + String.format("%.2f", dy).substring(1)
        }
    }

    //設定値を読み込み、アプリケーションセッションの変数に格納
    private fun readConfig() {
        val pref = getSharedPreferences(mCommonVar.c_configName, Context.MODE_PRIVATE)
        mCommonVar.connectMethod = pref.getInt(mCommonVar.c_configKey_connectMethod, mCommonVar.c_method_bluetooth)
        mCommonVar.sendMax = pref.getInt(mCommonVar.c_configKey_sendMax, mCommonVar.c_defaultSendMax)
        mCommonVar.uuid = pref.getString(mCommonVar.c_configKey_uuid, mCommonVar.c_defaultUUID).toString()
        mCommonVar.uuid_service = pref.getString(mCommonVar.c_configKey_uuid_service, mCommonVar.c_defaultUUID_service).toString()
        mCommonVar.uuid_characteristic = pref.getString(mCommonVar.c_configKey_uuid_characteristic, mCommonVar.c_defaultUUID_characteristic).toString()
        mCommonVar.xAxisRightPositive = pref.getBoolean(mCommonVar.c_configKey_xAxisRightPositive, true)
        mCommonVar.yAxisUpPositive = pref.getBoolean(mCommonVar.c_configKey_yAxisRightPositive, true)
        mCommonVar.threshold = pref.getInt(mCommonVar.c_configKey_threshold, mCommonVar.c_defaultThreshold)
        mCommonVar.threshold_array = getResources().getStringArray(R.array.setting_threshold_array)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("g_showTitleFlg",mShowTitleFlg)
        outState.putBoolean("g_showedTitleFlg",mShowedTitleFlg)
    }

    //ペリフェラル通知受信用ビューモデルとGATTサーバのコールバックはMainとBLE接続一覧で使うのでstaticにする
    companion object {
        var gBleNotifyViewModel: BleNotifyViewModel? = null
    }
}