package com.wxson.tws_player

import android.bluetooth.BluetoothDevice
import android.util.Log

/**
 * 配对指定的蓝牙设备
 */
object BluetoothUtils {

    fun breakPair(bluetoothDevice: BluetoothDevice) {
        try {
            val removeBond = BluetoothDevice::class.java.getMethod("removeBond")
            removeBond.invoke(bluetoothDevice)
        } catch (e: Exception) {
            Log.e("BlueUtils", e.message ?: "")
        }
    }

}