package com.sys_ky.ble_test

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ScanAdapter(private val onClick: (ScanInfo) -> Unit) : RecyclerView.Adapter<ScanAdapter.ScanViewHolder>() {

    private var mRecyclerView: RecyclerView? = null
    private var mScanList: List<ScanInfo>? = null

    class ScanViewHolder(view: View, val onClick: (ScanInfo) -> Unit): RecyclerView.ViewHolder(view) {
        val scanAddressTextView = view.findViewById<TextView>(R.id.scan_address_text)
        val scanNameTextView = view.findViewById<TextView>(R.id.scan_name_text)
        val scanRSSITextView = view.findViewById<TextView>(R.id.scan_rssi_text)
        val scanRSSIImageView = view.findViewById<ImageView>(R.id.scan_rssi_image)
        private var currentScanInfo: ScanInfo? = null

        init {
            view.setOnClickListener {
                currentScanInfo?.let {
                    onClick(it)
                }
            }
        }

        @SuppressLint("MissingPermission")
        fun bind(scanInfo: ScanInfo) {
            currentScanInfo = scanInfo
            scanAddressTextView.text = scanInfo.device.address
            scanNameTextView.text = scanInfo.device.name
            scanRSSITextView.text = scanInfo.rssi.toString()
            if(scanInfo.rssi < -80) {
                scanRSSIImageView.setImageResource(R.drawable.rssi0)
            } else if(scanInfo.rssi < -60) {
                scanRSSIImageView.setImageResource(R.drawable.rssi1)
            } else if(scanInfo.rssi < -40) {
                scanRSSIImageView.setImageResource(R.drawable.rssi2)
            } else {
                scanRSSIImageView.setImageResource(R.drawable.rssi3)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.scan_item, parent, false)
        return ScanViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        if(mScanList != null) {
            val scan = mScanList!![position]
            holder.bind(scan)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    fun setScanList(scanList: List<ScanInfo>) {
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

class ScanDiffCallback constructor(old:List<ScanInfo>,new:List<ScanInfo>) : DiffUtil.Callback() {
    private val oldList: List<ScanInfo>
    private val newList: List<ScanInfo>

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
        val oldItemAddress: String = oldList[oldItemPosition].device.address
        val newItemAddress: String = newList[newItemPosition].device.address

        return oldItemAddress == newItemAddress
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].equals(newList[newItemPosition])
    }
}