package com.wxson.tws_transmitter

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothReceiver(private val deviceList: MutableList<BluetoothDevice>) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        when (intent.action) {
            BluetoothDevice.ACTION_FOUND -> {
                //发现设备
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null){
                    deviceList.add(device)
//                    myAdapter.refresh(deviceList)
                }


            }
        }
        TODO("BluetoothReceiver.onReceive() is not implemented")
    }
}
