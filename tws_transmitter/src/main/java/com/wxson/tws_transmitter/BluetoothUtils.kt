package com.wxson.tws_transmitter

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.io.IOException
import java.util.*


/**
 * 配对指定的蓝牙设备
 */
 object BluetoothUtils {

    private val uuid = "00001101-0000-1000-8000-00805F9B34FB"
    private var pairingHandler: Handler? = null
    private lateinit var socket : BluetoothSocket

    fun makePair(bluetoothDevice: BluetoothDevice) {
        if (null == pairingHandler) {
            val handlerThread = HandlerThread("other_thread")
            handlerThread.start()
            pairingHandler = Handler(handlerThread.looper)
        }
        pairingHandler?.post(Runnable {
            getSocket(bluetoothDevice) //取得socket
            try {
                socket.connect() //请求配对
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
    }

    fun breakPair(bluetoothDevice: BluetoothDevice) {
        try {
            val removeBond = BluetoothDevice::class.java.getMethod("removeBond")
            removeBond.invoke(bluetoothDevice)
        } catch (e: Exception) {
            Log.e("BlueUtils", e.message ?: "")
        }
    }

    private fun getSocket(bluetoothDevice: BluetoothDevice) {
        var temp : BluetoothSocket? = null
        try {
            temp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid))
            //怪异错误： 直接赋值给socket,对socket操作可能出现异常，  要通过中间变量temp赋值给socket
        } catch (e: IOException) {
            e.printStackTrace()
        }
        socket = temp!!
    }
}