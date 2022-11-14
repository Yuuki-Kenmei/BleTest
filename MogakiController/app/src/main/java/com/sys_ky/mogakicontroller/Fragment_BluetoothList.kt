package com.sys_ky.mogakicontroller

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.app.ActivityCompat
import com.sys_ky.mogakicontroller.common.CommonVar

class Fragment_BluetoothList : Fragment() {

    private var mCommonVar: CommonVar = CommonVar.getInstance()

    //フラグメント内のイベント用リスナー
    lateinit var onItemClickListener: OnItemClickListener
    //フラグメント内のイベントで実行する関数を含むインターフェース
    interface OnItemClickListener {
        fun onItemClickBluetoothList(pos: Int)
    }

    var g_pairedDevices: Set<BluetoothDevice>? = null

    //リスナーを変数にセット
    override fun onAttach(context: Context) {
        super.onAttach(context)

        onItemClickListener = context as OnItemClickListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_bluetooth_list, container, false)

        if(Build.VERSION.SDK_INT <= 30 || ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            //ペアリング済デバイス一覧
            g_pairedDevices = mCommonVar.bluetoothAdapter?.bondedDevices
            if(g_pairedDevices != null) {
                val list: MutableList<String> = mutableListOf()
                g_pairedDevices?.forEach { device ->
                    list.add(device.name)
                }
                val adapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_list_item_1, list)

                val bluetoothListView = view.findViewById<ListView>(R.id.bluetoothList_listView)
                bluetoothListView.adapter = adapter
                bluetoothListView.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l ->
                    onItemClickListener.onItemClickBluetoothList(i)
                })
            }
        }

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_bluetooth_list, container, false)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Fragment_BluetoothList().apply {
                arguments = Bundle().apply {
                }
            }
    }
}