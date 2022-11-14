package com.sys_ky.mogakicontroller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.os.postDelayed
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sys_ky.mogakicontroller.ble.BlePeripheral
import com.sys_ky.mogakicontroller.ble.BleScanDataAdapter
import com.sys_ky.mogakicontroller.ble.BleScanDataViewModel
import com.sys_ky.mogakicontroller.ble.BleScanListViewModelFactory
import com.sys_ky.mogakicontroller.common.CommonVar

class Fragment_BleList : Fragment() {

    private var mCommonVar: CommonVar = CommonVar.getInstance()
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    lateinit var mHandler: Handler

    private val mBlePeripheralList: MutableList<BlePeripheral> = mutableListOf<BlePeripheral>()
    var mTryConnectFlg: Boolean = false

    private val mBleScanDataViewModel by viewModels<BleScanDataViewModel> {
        BleScanListViewModelFactory(this.requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_ble_list, container, false)

        val bleScanAdapter = BleScanDataAdapter{ blePeripheral -> adapterOnClick(blePeripheral) }
        val recycleView = view.findViewById<RecyclerView>(R.id.bleList_recycleView)
        recycleView.adapter = bleScanAdapter

        mBleScanDataViewModel.getScanLiveData().observe(this.requireActivity()) {
            it?.let {
                bleScanAdapter.setScanList(it)
            }
        }

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.bleList_swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            startScan()
        })

        mBluetoothLeScanner = mCommonVar.bluetoothAdapter?.bluetoothLeScanner
        mHandler = Handler(Looper.getMainLooper())

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_ble_list, container, false)
        return view
    }

    override fun onStart() {
        super.onStart()

        //スキャン
        startScan()
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        mHandler.removeCallbacksAndMessages(null)
        mBluetoothLeScanner?.stopScan(mScanCallback)
        this.activity?.findViewById<SwipeRefreshLayout>(R.id.bleList_swipeRefreshLayout)?.isRefreshing = false
    }

    private fun startScan() {

        if ((Build.VERSION.SDK_INT > 30 && ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) ||
            ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            this.activity?.findViewById<SwipeRefreshLayout>(R.id.bleList_swipeRefreshLayout)?.isRefreshing = false
            val dialog = AlertDialog.Builder(this.requireContext())
                .setTitle(R.string.alert_title_error)
                .setMessage(R.string.alert_message_permission_denied)
                .setPositiveButton(
                    R.string.alert_button_ok,
                    DialogInterface.OnClickListener { dialogInterface, i -> })
                .show()
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//            .setReportDelay(500)
            .build()
        try {
            mHandler.removeCallbacksAndMessages(null)
            mBluetoothLeScanner?.stopScan(mScanCallback)
            this.activity?.findViewById<SwipeRefreshLayout>(R.id.bleList_swipeRefreshLayout)?.isRefreshing = false

            mHandler.postDelayed({
                mBluetoothLeScanner?.stopScan(mScanCallback)
                this.activity?.findViewById<SwipeRefreshLayout>(R.id.bleList_swipeRefreshLayout)?.isRefreshing = false
            }, 5000)
            mBlePeripheralList.clear()
            mBleScanDataViewModel.clearBlePeripheralList()
            mBluetoothLeScanner?.startScan(null, settings, mScanCallback)
            this.activity?.findViewById<SwipeRefreshLayout>(R.id.bleList_swipeRefreshLayout)?.isRefreshing = true
        } catch (e: IllegalStateException) {

        }
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            //Log.d("","onScanResult")
            makeScanList(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            //Log.d("","onBatchScanResults:" + results.size.toString())
            makeScanList(results)
        }

        override fun onScanFailed(errorCode: Int) {
            //Log.d("","onScanFailed")
        }
    }

    private fun makeScanList(result: ScanResult) {
        makeScanList_sub(result)
    }
    private fun makeScanList(results: List<ScanResult>) {
        results.forEach { result ->
            makeScanList_sub(result)
        }
    }
    @SuppressLint("MissingPermission")
    private fun makeScanList_sub(result: ScanResult) {
        if(result.device == null) {
            return
        } else {
            var count: Int = 0
            var index: Int = -1
            //val nowBleScanList = mBleScanDataViewModel.getScanInfoList()
            //nowBleScanList.forEach {
            mBlePeripheralList.forEach { blePeripheral ->
                if(blePeripheral.getAddress() == result.device.address) {
                    return
                }
                if(index < 0) {
                    var name = result.device.name
                    if(name.isNullOrEmpty()) {
                        name = result.scanRecord?.deviceName
                    }
                    if(!name.isNullOrEmpty()) {
                        if(blePeripheral.getName().isNullOrEmpty()) {
                            index = count
                        } else if(blePeripheral.getName() >= name) {
                            index = count
                        }
                    } else if(blePeripheral.getAddress() >= result.device.address) {
                        if(blePeripheral.getName().isNullOrEmpty()) {
                            index = count
                        }
                    }
                }
                count++
            }
            if(index < 0) {
                //index = nowBleScanList.size
                index = mBlePeripheralList.size
            }
            val blePeripheral = BlePeripheral(result)
            mBlePeripheralList.add(index, blePeripheral)
            mBleScanDataViewModel.setBlePeripheralList(blePeripheral, index)
            return
        }
    }

    private fun adapterOnClick(blePeripheral: BlePeripheral) {
        if (Build.VERSION.SDK_INT > 30 && ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            val dialog = AlertDialog.Builder(this.requireContext())
                .setTitle(R.string.alert_title_error)
                .setMessage(R.string.alert_message_permission_denied)
                .setPositiveButton(
                    R.string.alert_button_ok,
                    DialogInterface.OnClickListener { dialogInterface, i -> })
                .show()
            return
        }
        if(!mTryConnectFlg) {
            mTryConnectFlg = true
            blePeripheral.connectGatt(this.requireContext())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            Fragment_BleList().apply {
                arguments = Bundle().apply {
                }
            }
    }
}