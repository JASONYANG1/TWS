package com.wxson.tws_player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class DeviceListAdapter : BaseAdapter() {
    private val listInAdapter: MutableList<BluetoothDeviceWithStatus> = ArrayList()

    fun refresh(list: List<BluetoothDeviceWithStatus>) {
        listInAdapter.clear()
        listInAdapter.addAll(list)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val viewHolder: ViewHolder?
        if (view == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(parent!!.context)
            view = inflater.inflate(R.layout.device_item, parent, false)
            viewHolder.nameTextView = view!!.findViewById(R.id.nameTextView)
            viewHolder.addressTextView = view.findViewById(R.id.addressTextView)
            viewHolder.statusTextView = view.findViewById(R.id.statusTextView)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        viewHolder.nameTextView?.text = listInAdapter[position].bluetoothDevice.name
        viewHolder.addressTextView?.text = listInAdapter[position].bluetoothDevice.address
//        viewHolder.statusTextView?.text = if (listInAdapter[position].status == Constants.BluetoothBonded) "已配对" else "-"
        viewHolder.statusTextView?.text =
        when (listInAdapter[position].status){
            Constants.BluetoothBonded -> "已配对"
            Constants.BluetoothNoBond -> "-"
            Constants.BluetoothConnected -> "已连接"
            else -> ""
        }
        return view
    }

    override fun getItem(position: Int): BluetoothDeviceWithStatus {
        return listInAdapter[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return listInAdapter.size
    }

    internal class ViewHolder {
        internal var nameTextView: TextView? = null
        internal var addressTextView: TextView? = null
        internal var statusTextView: TextView? = null
    }
}