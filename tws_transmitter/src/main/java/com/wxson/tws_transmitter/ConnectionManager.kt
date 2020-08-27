package com.wxson.tws_transmitter

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.util.*

class ConnectionManager(private var device: BluetoothDevice) {
    private val uuid = "00001101-0000-1000-8000-00805F9B34FB"
    private lateinit var socket : BluetoothSocket

    init {
        SendThread().start()
    }

    inner class SendThread : Thread() {
        override fun run() {
            super.run()
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid))
                socket.connect()
                Log.i("###connect", "连接成功")
                val os: OutputStream = socket.outputStream
                os.write("蓝牙已连接".toByteArray())
                os.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}