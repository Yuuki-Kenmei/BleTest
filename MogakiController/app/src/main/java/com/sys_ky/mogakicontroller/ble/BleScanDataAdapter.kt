package com.sys_ky.mogakicontroller.ble

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sys_ky.mogakicontroller.R

class BleScanDataAdapter(private val onClick: (BlePeripheral) -> Unit) : RecyclerView.Adapter<BleScanDataAdapter.BleScanDataViewHolder>() {
    private var mRecyclerView: RecyclerView? = null
    private var mScanList: List<BlePeripheral>? = null

    class BleScanDataViewHolder(view: View, val onClick: (BlePeripheral) -> Unit): RecyclerView.ViewHolder(view) {
        val scanNameTextView = view.findViewById<TextView>(R.id.bleListItem_textView_name)
        val scanAddressTextView = view.findViewById<TextView>(R.id.bleListItem_textView_address)
        val scanRSSIImageView = view.findViewById<ImageView>(R.id.bleListItem_imageView_rssi)
        private var currentBlePeripheral: BlePeripheral? = null

        init {
            view.setOnClickListener {
                currentBlePeripheral?.let {
                    onClick(it)
                }
            }
        }

        @SuppressLint("MissingPermission")
        fun bind(blePeripheral: BlePeripheral) {
            currentBlePeripheral = blePeripheral

            //名前が取れるなら名前とアドレス、取れないなら名前の場所にアドレス表示だけ
            var name = blePeripheral.getName()
            var address = "(" + blePeripheral.getAddress() + ")"
            if(name.isNullOrEmpty()) {
                name = blePeripheral.getAddress()
                address = ""
            }
            scanNameTextView.text = name
            scanAddressTextView.text = address

            //RSSIで適度にアンテナ画像表示
            if(blePeripheral.getRssi() < -80) {
                scanRSSIImageView.setImageResource(R.drawable.rssi0)
            } else if(blePeripheral.getRssi() < -60) {
                scanRSSIImageView.setImageResource(R.drawable.rssi1)
            } else if(blePeripheral.getRssi() < -40) {
                scanRSSIImageView.setImageResource(R.drawable.rssi2)
            } else {
                scanRSSIImageView.setImageResource(R.drawable.rssi3)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleScanDataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ble_list_item, parent, false)
        return BleScanDataViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: BleScanDataViewHolder, position: Int) {
        if(mScanList != null) {
            val scan = mScanList!![position]
            holder.bind(scan)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    fun setScanList(scanList: List<BlePeripheral>) {
        var diffResult: DiffUtil.DiffResult? = null
        if(mScanList == null) {
            mScanList = scanList
            notifyItemRangeInserted(0, if(scanList == null) 0 else scanList.size)
        }
        else {
            var callback = ScanDiffCallback(mScanList!!, scanList)
            diffResult = DiffUtil.calculateDiff(callback)
            mScanList = scanList
        }

        val layoutManager: RecyclerView.LayoutManager? = mRecyclerView?.getLayoutManager()
        if (layoutManager != null) {
            val recyclerViewState = layoutManager.onSaveInstanceState()
            diffResult?.dispatchUpdatesTo(this)
            layoutManager.onRestoreInstanceState(recyclerViewState)
        }
    }

    override fun getItemCount(): Int {
        var rtn = 0
        if(mScanList !=  null) {
            rtn = mScanList!!.size
        }
        return rtn
    }
}

class ScanDiffCallback constructor(old:List<BlePeripheral>, new:List<BlePeripheral>) : DiffUtil.Callback() {
    private val oldList: List<BlePeripheral>
    private val newList: List<BlePeripheral>

    init {
        oldList = old
        newList = new
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItemAddress: String = oldList[oldItemPosition].getAddress()
        val newItemAddress: String = newList[newItemPosition].getAddress()

        return oldItemAddress == newItemAddress
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].equals(newList[newItemPosition])
    }
}