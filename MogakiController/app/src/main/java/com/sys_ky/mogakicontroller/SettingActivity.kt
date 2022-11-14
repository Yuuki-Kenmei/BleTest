package com.sys_ky.mogakicontroller

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.sys_ky.mogakicontroller.common.CommonVar

class SettingActivity : AppCompatActivity() {

    private var mCommonVar: CommonVar = CommonVar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        //region 画面項目の内容およびイベント設定

        //ツールバーの設定
        var toolbar = findViewById<Toolbar>(R.id.setting_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Bluetoothトグルボタン
        val toggleButton_bluetooth = findViewById<ToggleButton>(R.id.setting_toggleButton_Bluetooth)
        toggleButton_bluetooth.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            toggleButtonChanged(compoundButton, b)
        })

        //BLEトグルボタン
        val toggleButton_ble = findViewById<ToggleButton>(R.id.setting_toggleButton_BLE)
        toggleButton_ble.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            toggleButtonChanged(compoundButton, b)
        })

        //UUIDテキスト
        val editText_uuid = findViewById<EditText>(R.id.setting_editText_uuid)
        editText_uuid.setOnFocusChangeListener(View.OnFocusChangeListener { view, b ->
            uuidFocusChanged(view, b)
        })
        editText_uuid.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->
            uuidEdited(textView, i, keyEvent)
        })

        //UUIDクリアボタン
        val button_uuid_clear = findViewById<Button>(R.id.setting_button_uuid_clear)
        button_uuid_clear.setOnClickListener(View.OnClickListener { button ->
            uuidClearButtonClick(button)
        })

        //UUIDデフォルトボタン
        val button_uuid_default = findViewById<Button>(R.id.setting_button_uuid_default)
        button_uuid_default.setOnClickListener(View.OnClickListener { button ->
            uuidDefaultButtonClick(button)
        })

        //UUID更新ボタン
        val button_uuid_apply = findViewById<Button>(R.id.setting_button_uuid_apply)
        button_uuid_apply.setOnClickListener(View.OnClickListener { button ->
            uuidApplyButtonClick(button)
        })

        //UUID_サービステキスト
        val editText_uuid_service = findViewById<EditText>(R.id.setting_editText_uuid_service)
        editText_uuid_service.setOnFocusChangeListener(View.OnFocusChangeListener { view, b ->
            uuidFocusChanged(view, b)
        })
        editText_uuid_service.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->
            uuidEdited(textView, i, keyEvent)
        })

        //UUID_サービスクリアボタン
        val button_uuid_service_clear = findViewById<Button>(R.id.setting_button_uuid_service_clear)
        button_uuid_service_clear.setOnClickListener(View.OnClickListener { button ->
            uuidClearButtonClick(button)
        })

        //UUID_サービスデフォルトボタン
        val button_uuid_service_default = findViewById<Button>(R.id.setting_button_uuid_service_default)
        button_uuid_service_default.setOnClickListener(View.OnClickListener { button ->
            uuidDefaultButtonClick(button)
        })

        //UUID_サービス更新ボタン
        val button_uuid_service_apply = findViewById<Button>(R.id.setting_button_uuid_service_apply)
        button_uuid_service_apply.setOnClickListener(View.OnClickListener { buttton ->
            uuidApplyButtonClick(buttton)
        })

        //UUID_キャラクタリスティックテキスト
        val editText_uuid_characteristic = findViewById<EditText>(R.id.setting_editText_uuid_characteristic)
        editText_uuid_characteristic.setOnFocusChangeListener(View.OnFocusChangeListener { view, b ->
            uuidFocusChanged(view, b)
        })
        editText_uuid_characteristic.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->
            uuidEdited(textView, i, keyEvent)
        })

        //UUID_キャラクタリスティッククリアボタン
        val button_uuid_characteristic_clear = findViewById<Button>(R.id.setting_button_uuid_characteristic_clear)
        button_uuid_characteristic_clear.setOnClickListener(View.OnClickListener { button ->
            uuidClearButtonClick(button)
        })

        //UUID_キャラクタリスティックデフォルトボタン
        val button_uuid_characteristic_default = findViewById<Button>(R.id.setting_button_uuid_characteristic_default)
        button_uuid_characteristic_default.setOnClickListener(View.OnClickListener { button ->
            uuidDefaultButtonClick(button)
        })

        //UUID_キャラクタリスティック更新ボタン
        val button_uuid_characteristic_apply = findViewById<Button>(R.id.setting_button_uuid_characteristic_apply)
        button_uuid_characteristic_apply.setOnClickListener(View.OnClickListener { buttton ->
            uuidApplyButtonClick(buttton)
        })

        //背景ビュー
        val background = findViewById<LinearLayout>(R.id.setting_background)
        background.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            backgroundTouched(motionEvent)
        })

        //送信値ピッカー1
        val numberPicker_sendMax1 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax1)
        numberPicker_sendMax1.minValue = 0
        numberPicker_sendMax1.maxValue = 1
        numberPicker_sendMax1.setOnValueChangedListener(NumberPicker.OnValueChangeListener { numberPicker, i, i2 ->
            sendMaxNumPickerChanged(i, i2, numberPicker_sendMax1)
        })

        //送信値ピッカー2
        val numberPicker_sendMax2 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax2)
        numberPicker_sendMax2.minValue = 0
        numberPicker_sendMax2.maxValue = 9
        numberPicker_sendMax2.setOnValueChangedListener(NumberPicker.OnValueChangeListener { numberPicker, i, i2 ->
            sendMaxNumPickerChanged(i, i2, numberPicker_sendMax2)
        })

        //送信値ピッカー3
        val numberPicker_sendMax3 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax3)
        numberPicker_sendMax3.minValue = 0
        numberPicker_sendMax3.maxValue = 9
        numberPicker_sendMax3.setOnValueChangedListener(NumberPicker.OnValueChangeListener { numberPicker, i, i2 ->
            sendMaxNumPickerChanged(i, i2, numberPicker_sendMax3)
        })

        //送信値ピッカー4
        val numberPicker_sendMax4 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax4)
        numberPicker_sendMax4.minValue = 0
        numberPicker_sendMax4.maxValue = 9
        numberPicker_sendMax4.setOnValueChangedListener(NumberPicker.OnValueChangeListener { numberPicker, i, i2 ->
            sendMaxNumPickerChanged(i, i2, numberPicker_sendMax4)
        })

        //送信値デフォルトボタン
        val button_sendMax_default = findViewById<Button>(R.id.setting_button_sendMax_default)
        button_sendMax_default.setOnClickListener(View.OnClickListener {
            sendMaxDefaultButtonClick()
        })

        //送信値更新ボタン
        val button_sendMax_apply = findViewById<Button>(R.id.setting_button_sendMax_apply)
        button_sendMax_apply.setOnClickListener(View.OnClickListener {
            sendMaxApplyButtonClick()
        })

        //X軸正負スイッチ
        val switch_xaxis = findViewById<Switch>(R.id.setting_switch_xaxis)
        switch_xaxis.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            xAxisSwitchChanged(compoundButton, b)
        })

        //Y軸正負スイッチ
        val switch_yaxis = findViewById<Switch>(R.id.setting_switch_yaxis)
        switch_yaxis.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            yAxisSwitchChanged(compoundButton, b)
        })

        //送信閾値スピナー
        val spinner_threshold = findViewById<Spinner>(R.id.setting_spinner_threshold)
        spinner_threshold.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                thresholdChanged(p0 as Spinner, p2, p3)
            }
        }

        //endregion

        //画面初期表示
        initDisplay()

    }

    //region 接続方式のイベント関数群

    //接続方式トグルボタン変更時
    private fun toggleButtonChanged(compoundButton: CompoundButton, checked: Boolean) {
        val toggleButton_bluetooth = findViewById<ToggleButton>(R.id.setting_toggleButton_Bluetooth)
        val toggleButton_ble = findViewById<ToggleButton>(R.id.setting_toggleButton_BLE)

        //現在BLEは未実装

        //if(compoundButton.id == R.id.setting_toggleButton_BLE) {
        //    if (checked) {
        //        val dialog = AlertDialog.Builder(this)
        //            .setTitle(R.string.alert_title_error)
        //            .setMessage("現在、BLEは未実装なので使用できません。")
        //            .setPositiveButton(
        //                R.string.alert_button_ok,
        //                DialogInterface.OnClickListener { dialogInterface, i -> })
        //            .show()
        //        toggleButton_ble.isChecked = false
        //        return
        //    }
        //}

        //変更されたのがBluetoothボタンの場合
        if(compoundButton.id == R.id.setting_toggleButton_Bluetooth) {
            if(checked) {
                //BluetoothをON、BLEをOFF
                toggleButton_bluetooth.setTextColor(Color.GREEN)
                toggleButton_bluetooth.alpha = 1.0f
                toggleButton_ble.setTextColor(Color.WHITE)
                toggleButton_ble.alpha = 0.5f
                toggleButton_ble.isChecked = false
                enableUUIDs(true)
            }
            else {
                //もともと選択されていた方をタッチしても切り替わらないようにしておく
                if(toggleButton_ble.isChecked == false) {
                    toggleButton_bluetooth.isChecked = true
                }
            }
        }
        //変更されたのがBLEボタンの場合
        else if(compoundButton.id == R.id.setting_toggleButton_BLE) {
            if(checked) {
                //BLEをON、BluetoothをOFF
                toggleButton_bluetooth.setTextColor(Color.WHITE)
                toggleButton_bluetooth.alpha = 0.5f
                toggleButton_ble.setTextColor(Color.GREEN)
                toggleButton_ble.alpha = 1.0f
                toggleButton_bluetooth.isChecked = false
                enableUUIDs(false)
            }
            else {
                //もともと選択されていた方をタッチしても切り替わらないようにしておく
                if(toggleButton_bluetooth.isChecked == false) {
                    toggleButton_ble.isChecked = true
                }
            }
        }

        //設定を保存
        if(checked) {
            if(toggleButton_bluetooth.isChecked && !toggleButton_ble.isChecked) {
                writeConfig(mCommonVar.c_configKey_connectMethod, mCommonVar.c_method_bluetooth)
            }
            if(!toggleButton_bluetooth.isChecked && toggleButton_ble.isChecked) {
                writeConfig(mCommonVar.c_configKey_connectMethod, mCommonVar.c_method_ble)
            }
        }
    }
    //endregion

    //region UUIDのイベント関数群

    //UUIDクリアボタン押下時
    private fun uuidClearButtonClick(view: View) {
        //キーボードを隠してテキストを空白に
        var editText: TextView? = null
        when(view.id) {
            R.id.setting_button_uuid_clear -> {
                editText = findViewById<TextView>(R.id.setting_editText_uuid)
            }
            R.id.setting_button_uuid_service_clear -> {
                editText = findViewById<TextView>(R.id.setting_editText_uuid_service)
            }
            R.id.setting_button_uuid_characteristic_clear -> {
                editText = findViewById<TextView>(R.id.setting_editText_uuid_characteristic)
            }
        }
        hideKeyboard(editText!!)
        editText.text = ""
    }

    //UUIDデフォルトボタン押下時
    private fun uuidDefaultButtonClick(view: View) {
        //キーボードを隠してテキストをデフォルトに
        var editText: TextView? = null
        var uuid_default: String = ""
        when(view.id) {
            R.id.setting_button_uuid_default -> {
                editText = findViewById<TextView>(R.id.setting_editText_uuid)
                uuid_default = mCommonVar.c_defaultUUID
            }
            R.id.setting_button_uuid_service_default -> {
                editText = findViewById<TextView>(R.id.setting_editText_uuid_service)
                uuid_default = mCommonVar.c_defaultUUID_service
            }
            R.id.setting_button_uuid_characteristic_default -> {
                editText = findViewById<TextView>(R.id.setting_editText_uuid_characteristic)
                uuid_default = mCommonVar.c_defaultUUID_characteristic
            }
        }
        hideKeyboard(editText!!)
        editText.text = uuid_default
    }

    //UUID更新ボタン押下時
    private fun uuidApplyButtonClick(view: View) {

        var editText: TextView? = null
        var configKey: String = ""
        when(view.id) {
            R.id.setting_button_uuid_apply -> {
                editText = findViewById<TextView>(R.id.setting_editText_uuid)
                configKey = mCommonVar.c_configKey_uuid
            }
            R.id.setting_button_uuid_service_apply -> {
                editText = findViewById<TextView>(R.id.setting_editText_uuid_service)
                configKey = mCommonVar.c_configKey_uuid_service
            }
            R.id.setting_button_uuid_characteristic_apply -> {
                editText = findViewById<TextView>(R.id.setting_editText_uuid_characteristic)
                configKey = mCommonVar.c_configKey_uuid_characteristic
            }
        }
        //キーボードを隠す
        hideKeyboard(editText!!)

        //UUIDが正しい書式「XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX」でない場合、エラー
        val uuid = editText.text.toString()
        val regex = Regex("[0-9a-zA-Z]{8}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{12}")
        if(!uuid.matches(regex)) {
            val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.alert_title_error)
                .setMessage(R.string.alert_message_uuid)
                .setPositiveButton(
                    R.string.alert_button_ok,
                    DialogInterface.OnClickListener { dialogInterface, i -> })
                .show()
            return
        }

        //設定を保存
        writeConfig(configKey, uuid)

        Toast.makeText(this, getString(R.string.toast_update), Toast.LENGTH_SHORT).show()
    }

    //UUIDフォーカス変更時
    private fun uuidFocusChanged(view: View, focused: Boolean) {
        if(!focused)
        {
            //キーボードを隠す
            hideKeyboard(view)
        }
    }

    //UUID変更時
    private fun uuidEdited(view: View, action:Int, keyEvent: KeyEvent?): Boolean {
        //確定でキーボードを隠す
        if(action == EditorInfo.IME_ACTION_DONE || action == EditorInfo.IME_ACTION_NEXT) {
            hideKeyboard(view)
        }
        return true
    }

    //背景タッチ時
    private fun backgroundTouched(motionEvent: MotionEvent): Boolean {
        //タッチ時、UUIDテキストにフォーカスされていた場合、キーボードを隠してフォーカスを外す
        when(motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                val editText_uuid = findViewById<EditText>(R.id.setting_editText_uuid)
                if(editText_uuid.isFocused) {
                    hideKeyboard(editText_uuid)
                }
                val editText_uuid_service = findViewById<EditText>(R.id.setting_editText_uuid_service)
                if(editText_uuid_service.isFocused) {
                    hideKeyboard(editText_uuid_service)
                }
                val editText_uuid_characteristic = findViewById<EditText>(R.id.setting_editText_uuid_characteristic)
                if(editText_uuid_characteristic.isFocused) {
                    hideKeyboard(editText_uuid_characteristic)
                }
            }
            else -> {

            }
        }
        return true
    }
    //endregion

    //region 送信値のイベント関数群

    //送信値ピッカー変更時
    private fun sendMaxNumPickerChanged(oldVal: Int, newVal: Int, view: NumberPicker) {
        val numberPicker_sendMax1 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax1)
        val numberPicker_sendMax2 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax2)
        val numberPicker_sendMax3 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax3)
        val numberPicker_sendMax4 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax4)

        //千の位を1にされた場合、他の位は0固定（最大1000なので）
        if(view.id == R.id.setting_numPicker_sendMax1) {
            if(newVal == 1) {
                numberPicker_sendMax2.value = 0
                numberPicker_sendMax3.value = 0
                numberPicker_sendMax4.value = 0
            }
        }
        else {
            //千の位以外が0でない場合、千の位は0固定（最大1000なので）
            if(newVal != 0) {
                numberPicker_sendMax1.value = 0
            }
        }
    }

    //送信値ピッカーの設定
    private fun sendMaxNumPickerSetting(value: Int) {
        val numberPicker_sendMax1 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax1)
        val numberPicker_sendMax2 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax2)
        val numberPicker_sendMax3 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax3)
        val numberPicker_sendMax4 = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax4)
        //千の位
        if(value.toString().length < 4) {
            numberPicker_sendMax1.value = 0
        }
        else
        {
            numberPicker_sendMax1.value = 1
        }
        //百の位
        if(value.toString().length < 3) {
            numberPicker_sendMax2.value = 0
        }
        else {
            numberPicker_sendMax2.value = value.toString().takeLast(3).substring(0, 1).toInt()
        }
        //十の位
        if(value.toString().length < 2) {
            numberPicker_sendMax3.value = 0
        }
        else {
            numberPicker_sendMax3.value = value.toString().takeLast(2).substring(0, 1).toInt()
        }
        //一の位
        if(value.toString().length < 1) {
            numberPicker_sendMax4.value = 0
        }
        else {
            numberPicker_sendMax4.value = value.toString().takeLast(1).substring(0, 1).toInt()
        }
    }

    //送信値デフォルトボタン押下時
    private fun sendMaxDefaultButtonClick() {
        //ピッカーをデフォルト値で設定
        sendMaxNumPickerSetting(mCommonVar.c_defaultSendMax)
    }

    //送信値更新ボタン押下時
    private fun sendMaxApplyButtonClick() {
        //ピッカーから値を作成
        val value = findViewById<NumberPicker>(R.id.setting_numPicker_sendMax1).value * 1000 +
                findViewById<NumberPicker>(R.id.setting_numPicker_sendMax2).value * 100 +
                findViewById<NumberPicker>(R.id.setting_numPicker_sendMax3).value * 10 +
                findViewById<NumberPicker>(R.id.setting_numPicker_sendMax4).value * 1

        //値が0の場合、エラー
        if(value == 0) {
            val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.alert_title_error)
                .setMessage(R.string.alert_message_sendmax_zero)
                .setPositiveButton(
                    R.string.alert_button_ok,
                    DialogInterface.OnClickListener { dialogInterface, i -> })
                .show()
            return
        }

        //設定を保存
        writeConfig(mCommonVar.c_configKey_sendMax, value)

        Toast.makeText(this, getString(R.string.toast_update), Toast.LENGTH_SHORT).show()
    }
    //endregion

    //region X,Y軸正負のイベント関数群

    //X軸正負スイッチ変更字
    private fun xAxisSwitchChanged(compoundButton: CompoundButton, boolean: Boolean) {
        //設定を保存
        writeConfig(mCommonVar.c_configKey_xAxisRightPositive, !boolean)
    }

    //Y軸正負スイッチ変更字
    private fun yAxisSwitchChanged(compoundButton: CompoundButton, boolean: Boolean) {
        //設定を保存
        writeConfig(mCommonVar.c_configKey_yAxisRightPositive, !boolean)
    }
    //endregion

    //region 送信閾値のイベント関数群
    private fun thresholdChanged(spinner: Spinner, position: Int, id: Long) {
        //設定を保存
        writeConfig(mCommonVar.c_configKey_threshold, spinner.selectedItem.toString().toInt())
    }

    //endregion

    //ツールバーの戻るボタン押下時用
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->  {
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    //キーボードを隠し、指定ビューからフォーカスを外す
    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        view.clearFocus()
    }

    //画面初期表示
    private fun initDisplay() {
        //現在の設定値で画面項目を設定する（設定を読み直しせず、アプリケーションセッションの値で表示）

        //接続方式
        if(mCommonVar.connectMethod == mCommonVar.c_method_bluetooth) {
            findViewById<ToggleButton>(R.id.setting_toggleButton_Bluetooth).isChecked = true
        }
        else if(mCommonVar.connectMethod == mCommonVar.c_method_ble) {
            findViewById<ToggleButton>(R.id.setting_toggleButton_BLE).isChecked = true
        }

        //UUID
        findViewById<TextView>(R.id.setting_editText_uuid).text = mCommonVar.uuid

        //UUID(サービス)
        findViewById<TextView>(R.id.setting_editText_uuid_service).text = mCommonVar.uuid_service

        //UUID(キャラクタリスティック)
        findViewById<TextView>(R.id.setting_editText_uuid_characteristic).text = mCommonVar.uuid_characteristic

        //UUIDの活性非活性制御
        enableUUIDs(findViewById<ToggleButton>(R.id.setting_toggleButton_Bluetooth).isChecked)

        //送信値
        sendMaxNumPickerSetting(mCommonVar.sendMax)

        //X軸正負
        findViewById<Switch>(R.id.setting_switch_xaxis).isChecked = !mCommonVar.xAxisRightPositive

        //Y軸正負
        findViewById<Switch>(R.id.setting_switch_yaxis).isChecked = !mCommonVar.yAxisUpPositive

        //送信値
        findViewById<Spinner>(R.id.setting_spinner_threshold).setSelection(
            mCommonVar.threshold_array.indexOf(
                mCommonVar.threshold.toString()))
    }

    private fun enableUUIDs(BluetoothOn: Boolean) {
        if(BluetoothOn) {
            findViewById<TextView>(R.id.setting_editText_uuid).isEnabled = true
            findViewById<TextView>(R.id.setting_button_uuid_clear).isEnabled = true
            findViewById<TextView>(R.id.setting_button_uuid_default).isEnabled = true
            findViewById<TextView>(R.id.setting_button_uuid_apply).isEnabled = true
            findViewById<TextView>(R.id.setting_editText_uuid_service).isEnabled = false
            findViewById<TextView>(R.id.setting_button_uuid_service_clear).isEnabled = false
            findViewById<TextView>(R.id.setting_button_uuid_service_default).isEnabled = false
            findViewById<TextView>(R.id.setting_button_uuid_service_apply).isEnabled = false
            findViewById<TextView>(R.id.setting_editText_uuid_characteristic).isEnabled = false
            findViewById<TextView>(R.id.setting_button_uuid_characteristic_clear).isEnabled = false
            findViewById<TextView>(R.id.setting_button_uuid_characteristic_default).isEnabled = false
            findViewById<TextView>(R.id.setting_button_uuid_characteristic_apply).isEnabled = false
        } else {
            findViewById<TextView>(R.id.setting_editText_uuid).isEnabled = false
            findViewById<TextView>(R.id.setting_button_uuid_clear).isEnabled = false
            findViewById<TextView>(R.id.setting_button_uuid_default).isEnabled = false
            findViewById<TextView>(R.id.setting_button_uuid_apply).isEnabled = false
            findViewById<TextView>(R.id.setting_editText_uuid_service).isEnabled = true
            findViewById<TextView>(R.id.setting_button_uuid_service_clear).isEnabled = true
            findViewById<TextView>(R.id.setting_button_uuid_service_default).isEnabled = true
            findViewById<TextView>(R.id.setting_button_uuid_service_apply).isEnabled = true
            findViewById<TextView>(R.id.setting_editText_uuid_characteristic).isEnabled = true
            findViewById<TextView>(R.id.setting_button_uuid_characteristic_clear).isEnabled = true
            findViewById<TextView>(R.id.setting_button_uuid_characteristic_default).isEnabled = true
            findViewById<TextView>(R.id.setting_button_uuid_characteristic_apply).isEnabled = true
        }
    }

    //設定を保存する
    private fun writeConfig(key: String, value: String){
        val pref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, value)
        editor.commit()
    }
    private fun writeConfig(key: String, value: Int){
        val pref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.commit()
    }
    private fun writeConfig(key: String, value: Boolean){
        val pref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

}