package com.wxson.tws_receiver.ui.main

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.wxson.tws_receiver.R

class DeviceListAdapter  : BaseAdapter() {
    private val privateList: MutableList<BluetoothDeviceItem> = ArrayList()

    fun refresh(list: List<BluetoothDeviceItem>) {
        privateList.clear()
        privateList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val viewHolder: ViewHolder?
        if (view == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(parent!!.context)
            view = inflater.inflate(R.layout.device_item, parent, false)
            viewHolder.deviceNameTextView = view!!.findViewById(R.id.deviceName)
            viewHolder.deviceAddressTextView = view.findViewById(R.id.deviceAddress)
            viewHolder.deviceBondStateTextView = view.findViewById(R.id.deviceBondState)
            viewHolder.deviceTypeTextView = view.findViewById(R.id.deviceType)
            viewHolder.deviceConnectState = view.findViewById(R.id.deviceConnectState)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        val deviceItem = privateList[position]
        viewHolder.deviceNameTextView?.text = deviceItem.device.name
        viewHolder.deviceAddressTextView?.text = deviceItem.device.address
        viewHolder.deviceBondStateTextView?.text =
            when  (deviceItem.device.bondState) {
                BluetoothDevice.BOND_BONDED -> "已绑定"
                BluetoothDevice.BOND_BONDING -> "绑定中"
                BluetoothDevice.BOND_NONE -> "未匹配"
                else -> ""
            }
        viewHolder.deviceTypeTextView?.text =
            when (deviceItem.device.type) {
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "传统蓝牙"
                BluetoothDevice.DEVICE_TYPE_DUAL -> "双模蓝牙"
                BluetoothDevice.DEVICE_TYPE_LE -> "低功耗蓝牙"
                BluetoothDevice.DEVICE_TYPE_UNKNOWN -> "未知类型"
                else -> ""
            }
        viewHolder.deviceConnectState?.text =
            when (deviceItem.connectState) {
                Constants.BluetoothConnected -> "已连接"
                Constants.BluetoothUnconnected -> "未连接"
                else -> ""
            }
        return view
    }

    override fun getItem(position: Int): BluetoothDeviceItem {
        return privateList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return privateList.size
    }

    internal class ViewHolder {
        internal var deviceNameTextView: TextView? = null
        internal var deviceAddressTextView: TextView? = null
        internal var deviceBondStateTextView: TextView? = null
        internal var deviceTypeTextView: TextView? = null
        internal var deviceConnectState: TextView? = null
    }

}