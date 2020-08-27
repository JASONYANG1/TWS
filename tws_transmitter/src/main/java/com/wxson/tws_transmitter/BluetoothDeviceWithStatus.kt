package com.wxson.tws_transmitter

import android.bluetooth.BluetoothDevice

class BluetoothDeviceWithStatus(device : BluetoothDevice) {
    val bluetoothDevice : BluetoothDevice = device
    var isPaired : Boolean = false
}