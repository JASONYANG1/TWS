package com.wxson.tws_transmitter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class DeviceListAdapter : BaseAdapter() {
    private val list: MutableList<BluetoothDevice> = ArrayList()

    fun refresh(list: List<BluetoothDevice>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var viewHolder : ViewHolder? = null
        if (view == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(parent!!.context)
            view = inflater.inflate(R.layout.device_item, parent, false)
            viewHolder.textView = view!!.findViewById(R.id.nameTextView)
            viewHolder.textView1 = view.findViewById(R.id.addressTextView)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        viewHolder.textView?.text = list[position].name
        viewHolder.textView1?.text = list[position].address
        return view
    }

    override fun getItem(position: Int): BluetoothDevice {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

    internal class ViewHolder {
        internal var textView: TextView? = null
        internal var textView1: TextView? = null
    }
}